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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * Factory for flat structures.
 */
public class StructureFactory<T> extends Factory<T> {

    private final static String FACTORY_NAME = "Structure";

    // FIXME
    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";

    public StructureFactory(final ClassPool pool) {
        super(pool);
    }

    public StructureFactory() {
        super();
    }

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    @Override
    public Class<?> createImpl(final Class<T> classDef) {
        verifyClassDefinition(classDef);

        try {
            final CtClass ctClass = getClassPool().makeClass(createClassName(classDef));
            ctClass.addInterface(getClassPool().getCtClass(classDef.getName()));

            // Internal fields.
            ctClass.addField(new CtField(CtClass.intType, X_FIELD, ctClass));
            ctClass.addField(new CtField(CtClass.intType, Y_FIELD, ctClass));

            // Getter for X.
            ctClass.addMethod(CtNewMethod.make("public int getX() { return this.x; }", ctClass));

            // Setter for X.
            ctClass.addMethod(CtNewMethod.make("public void setX(int x) { this.x = x; }", ctClass));

            // Getter for Y.
            ctClass.addMethod(CtNewMethod.make("public int getY() { return this.y; }", ctClass));

            // Setter for Y.
            ctClass.addMethod(CtNewMethod.make("public void setY(int y) { this.y = y; }", ctClass));

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
