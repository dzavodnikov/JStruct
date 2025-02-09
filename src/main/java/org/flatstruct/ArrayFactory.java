/*
 *     Copyright 2025 The FlatStruct Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flatstruct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

public class ArrayFactory<T> extends Factory<T> {

    private final static String FACTORY_NAME = "Array";

    private final int size;

    public ArrayFactory(final int size, final ClassPool pool) {
        super(pool);

        if (size < 0) {
            throw new IllegalArgumentException("Size of array can't be negative");
        }
        this.size = size;
    }

    public ArrayFactory(final int size) {
        this(size, ClassPool.getDefault());
    }

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    /**
     * @return size of array.
     */
    public int size() {
        return this.size;
    }

    protected void initializeFields(final CtClass ctClass, final Map<String, Class<?>> fields,
            final Class<T> classDef) throws NotFoundException, CannotCompileException {
        for (java.lang.reflect.Field field : classDef.getFields()) {
            if (field.isAnnotationPresent(Field.class)) {
                final Field fieldMeta = field.getAnnotation(Field.class);

                verifyFieldType(field);

                final String fieldName = getFieldName(fields, field);
                final Class<?> fieldType = getFieldType(fieldMeta);
                fields.put(fieldName, fieldType);

                int modifiers = Modifier.PRIVATE;
                modifiers |= fieldMeta.isVolatile() ? Modifier.VOLATILE : 0;

                final CtField ctField = new CtField(
                        getClassPool().getCtClass(fieldType.getName()),
                        fieldName,
                        ctClass);
                ctField.setModifiers(modifiers);
                ctClass.addField(ctField);
            }
        }
    }

    private void verifyFieldType(final Map<String, Class<?>> fields, final String fieldName,
            final Class<?> expectedType) {
        final Class<?> fieldType = fields.get(fieldName);
        if (fieldType == null) {
            throw new RuntimeException(String.format("Field '%s' is not exists", fieldName));
        }

        if (!fieldType.equals(expectedType)) {
            throw new RuntimeException(String.format("Field '%s' has type %s instead of %s",
                    fieldName, fieldType.getName(), expectedType.getName()));
        }
    }

    protected <V> void initializeGettersAndSetters(final CtClass ctClass, final Map<String, Class<?>> fields,
            final Class<T> classDef) throws CannotCompileException, NotFoundException {
        for (Method method : classDef.getMethods()) {
            final MethodBuilder mb = new MethodBuilder(method.getName());
            mb.addModifier("public");

            boolean wasAnnotated = false;

            for (Parameter param : method.getParameters()) {
                final Setter setterAnnotation = param.getAnnotation(Setter.class);
                if (setterAnnotation != null) {
                    wasAnnotated = true;

                    final String fieldName = setterAnnotation.value();
                    final Class<?> paramType = param.getType();
                    verifyFieldType(fields, fieldName, paramType);

                    mb.addArgument(param);

                    String body = "this.%s = %s;";
                    if (setterAnnotation.isSynchronized()) {
                        body = String.format("synchronized(this) { %s }", body);
                    }
                    mb.addBodyLine(body, fieldName, param.getName());
                }
            }

            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                wasAnnotated = true;

                final String fieldName = getterAnnotation.value();
                final Class<?> returnType = method.getReturnType();
                verifyFieldType(fields, fieldName, returnType);

                if (getterAnnotation.isSynchronized()) {
                    mb.addModifier("synchronized");
                }

                mb.setReturnType(returnType);
                mb.addBodyLine("return this.%s;", getterAnnotation.value());
            }

            if (wasAnnotated) {
                mb.addMethodTo(ctClass);
            }
        }
    }

    protected MethodBuilder initializeEquals(final String className, final Map<String, Class<?>> fields)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("equals");
        mb.addModifier("public");
        mb.addArgument(Object.class, "obj");
        mb.setReturnType(boolean.class);

        mb.addBodyLine("if (null == obj) return false;");
        mb.addBodyLine("if (this == obj) return true;");
        mb.addBodyLine("if (getClass() != obj.getClass()) return false;");

        mb.addBodyLine("%s struct = (%s) obj;", className, className);
        for (String fieldName : fields.keySet()) {
            if (fields.get(fieldName).isPrimitive()) {
                mb.addBodyLine("if (this.%s != struct.%s) return false;", fieldName, fieldName);
            } else {
                mb.addBodyLine("if (this.%s != null) {", fieldName);
                mb.addBodyLine("    if (!this.%s.equals(struct.%s)) return false;", fieldName, fieldName);
                mb.addBodyLine("} else {");
                mb.addBodyLine("    if (struct.%s != null) return false;", fieldName, fieldName);
                mb.addBodyLine("}");
            }
        }
        mb.addBodyLine("return true;");

        return mb;
    }

    protected MethodBuilder initializeHashCode(final Map<String, Class<?>> fields)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("hashCode");
        mb.addModifier("public");
        mb.setReturnType(int.class);

        mb.addBodyLine("final int prime = 31;");
        mb.addBodyLine("int result = 1;");
        for (String fieldName : fields.keySet()) {
            if (fields.get(fieldName).isPrimitive()) {
                mb.addBodyLine("result = prime * result + (int) this.%s;", fieldName);
            } else {
                mb.addBodyLine("result = prime * result + ((this.%s == null) ? 0 : this.%s.hashCode());",
                        fieldName, fieldName);
            }
        }
        mb.addBodyLine("return result;");

        return mb;
    }

    protected MethodBuilder initializeToString(final String className, final Map<String, Class<?>> fields)
            throws CannotCompileException, NotFoundException {
        final MethodBuilder mb = new MethodBuilder("toString");
        mb.addModifier("public");
        mb.setReturnType(String.class);

        mb.addBodyLine("final StringBuilder sb = new StringBuilder();");
        mb.addBodyLine("sb.append(\"%s [\\n\");", className);
        for (String fieldName : fields.keySet()) {
            mb.addBodyLine("sb.append(\"    %s=\");", fieldName);
            if (fields.get(fieldName).isPrimitive()) {
                mb.addBodyLine("sb.append(this.%s);", fieldName);
            } else {
                mb.addBodyLine("if (this.%s != null) {", fieldName);
                mb.addBodyLine("    sb.append(\"<\");");
                mb.addBodyLine("    sb.append(this.%s == null ? 'null' : ((Object) this.%s).hashCode());",
                        fieldName, fieldName);
                mb.addBodyLine("    sb.append(\">\");");
                mb.addBodyLine("} else {");
                mb.addBodyLine("    sb.append(\"null\");");
                mb.addBodyLine("}");
            }
            mb.addBodyLine("sb.append(\"\\n\");");
        }
        mb.addBodyLine("sb.append(\"]\");");
        mb.addBodyLine("return sb.toString();");

        return mb;
    }

    @Override
    public Class<?> createImpl(final Class<T> classDef) {
        verifyClassDefinition(classDef);

        try {
            final String newClassName = createClassName(classDef);
            final CtClass ctClass = getClassPool().makeClass(newClassName);
            ctClass.addInterface(getClassPool().getCtClass(classDef.getName()));

            final Map<String, Class<?>> fields = new HashMap<>();
            initializeFields(ctClass, fields, classDef);
            initializeGettersAndSetters(ctClass, fields, classDef);
            initializeEquals(newClassName, fields).addMethodTo(ctClass);
            initializeHashCode(fields).addMethodTo(ctClass);
            initializeToString(newClassName, fields).addMethodTo(ctClass);

            ctClass.detach();
            return ctClass.toClass(classDef);
        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
}
