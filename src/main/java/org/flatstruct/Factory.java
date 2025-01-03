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

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;

/**
 * Base class for all factories.
 */
public abstract class Factory<T> {

    private final ClassPool pool;

    protected Factory(final ClassPool pool) {
        this.pool = pool;
    }

    public Factory() {
        this(ClassPool.getDefault());
    }

    public abstract String getFactoryName();

    protected ClassPool getClassPool() {
        return this.pool;
    }

    /**
     * Cache of class implementations.
     */
    private static final Map<Class<?>, Class<?>> CLASS_IMPL_CACHE = new HashMap<>();

    /**
     * @param classDef describes how to data should be located into the memory;
     * @return class name of new structure.
     */
    protected String createClassName(final Class<T> classDef) {
        return String.format("%s.%s_of_%s", classDef.getPackage().getName(), getFactoryName(),
                classDef.getSimpleName());
    }

    protected void verifyClassDefinition(final Class<T> classDef) {
        if (classDef == null) {
            throw new IllegalArgumentException("Class definition parameter should not be null");
        }
        if (!classDef.isInterface()) {
            throw new IllegalArgumentException("Class definition parameter should be an interface");
        }
    }

    /**
     * @param classDef describes how to data should be located into the memory;
     * @return created class implementation based on class definition.
     */
    abstract public Class<?> createImpl(Class<T> classDef);

    /**
     * Create new implementation of data structure based on it's definition.
     *
     * @param classDef class definition of data structure;
     * @return class implementation based on class definition.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getImpl(final Class<T> classDef) {
        Class<?> classImpl = CLASS_IMPL_CACHE.get(classDef);
        if (classImpl == null) {
            classImpl = createImpl(classDef);
            CLASS_IMPL_CACHE.put(classDef, classImpl);
        }
        return (Class<T>) classImpl;
    }
}
