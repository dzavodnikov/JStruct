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
package org.flatstruct.example.structure;

import org.flatstruct.Getter;
import org.flatstruct.Setter;

/**
 * Testing structure.
 */
public interface Person {

    String NAME_FIELD_NAME = "name";
    String AGE_FIELD_NAME = "age";

    void setName(@Setter(NAME_FIELD_NAME) String name);

    @Getter(NAME_FIELD_NAME)
    String getName();

    void setAge(@Setter(AGE_FIELD_NAME) int age);

    @Getter(AGE_FIELD_NAME)
    int getAge();
}
