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

import java.lang.reflect.InvocationTargetException;
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

/**
 * Factory for flat structures.
 */
public class StructureFactory<T> extends Factory<T> {

    private final static String FACTORY_NAME = "Structure";

    public StructureFactory(final ClassPool pool) {
        super(pool);
    }

    public StructureFactory() {
        this(ClassPool.getDefault());
    }

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
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
                    mb.addBodyLine("this.%s = %s;", fieldName, param.getName());
                }
            }

            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                wasAnnotated = true;

                final String fieldName = getterAnnotation.value();
                final Class<?> returnType = method.getReturnType();
                verifyFieldType(fields, fieldName, returnType);

                mb.setReturnType(returnType);
                mb.addBodyLine("return this.%s;", getterAnnotation.value());
            }

            if (wasAnnotated) {
                mb.addMethodTo(ctClass);
            }
        }
    }

    @Override
    public Class<?> createImpl(final Class<T> classDef) {
        verifyClassDefinition(classDef);

        try {
            final CtClass ctClass = getClassPool().makeClass(createClassName(classDef));
            ctClass.addInterface(getClassPool().getCtClass(classDef.getName()));

            final Map<String, Class<?>> fields = new HashMap<>();
            initializeFields(ctClass, fields, classDef);
            initializeGettersAndSetters(ctClass, fields, classDef);

            ctClass.detach();
            return ctClass.toClass(classDef);
        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param classDef describes how to data should be located into the memory;
     * @return structure instance that was generated from class definition.
     */
    public T create(final Class<T> classDef) {
        verifyClassDefinition(classDef);

        try {
            return classDef.cast(getImpl(classDef).getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
