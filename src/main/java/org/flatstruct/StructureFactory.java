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
import java.util.HashSet;
import java.util.Set;

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

    private final int modifiers;

    public StructureFactory(final ClassPool pool, final int... modifiers) {
        super(pool);

        // Join modifiers in one integer.
        int join = 0;
        for (int m : modifiers) {
            join |= m;
        }
        this.modifiers = join;
    }

    /**
     * @param modifiers field default <a href=
     *                  "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Modifier.html">modifiers</a>.
     */
    public StructureFactory(final int... modifiers) {
        this(ClassPool.getDefault(), modifiers);
    }

    public StructureFactory() {
        this(Modifier.PRIVATE);
    }

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    protected void addField(final CtClass ctClass, final Set<String> createdFields,
            final Class<?> fieldType, final String fieldName) throws NotFoundException, CannotCompileException {
        final CtField field = new CtField(getClassPool().getCtClass(fieldType.getName()), fieldName, ctClass);
        field.setModifiers(this.modifiers);

        if (!createdFields.contains(fieldName)) {
            createdFields.add(fieldName);

            ctClass.addField(field);
        }
    }

    protected void initializeClass(final CtClass ctClass, final Class<T> classDef)
            throws CannotCompileException, NotFoundException {
        final Set<String> createdFields = new HashSet<>();

        for (Method method : classDef.getMethods()) {
            final MethodBuilder mb = new MethodBuilder(method.getName());
            mb.addModifier("public");

            boolean wasAnnotated = false;

            for (Parameter param : method.getParameters()) {
                final Setter setterAnnotation = param.getAnnotation(Setter.class);
                if (setterAnnotation != null) {
                    wasAnnotated = true;

                    final String fieldName = setterAnnotation.value();

                    addField(ctClass, createdFields, param.getType(), fieldName);

                    mb.addArgument(param);
                    mb.addBodyLine("this.%s = %s;", fieldName, param.getName());
                }
            }

            final Getter getterAnnotation = method.getAnnotation(Getter.class);
            if (getterAnnotation != null) {
                wasAnnotated = true;

                final String fieldName = getterAnnotation.value();

                addField(ctClass, createdFields, method.getReturnType(), fieldName);

                mb.setReturnType(method.getReturnType());
                mb.addBodyLine("return this.%s;", fieldName);
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

            initializeClass(ctClass, classDef);

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
