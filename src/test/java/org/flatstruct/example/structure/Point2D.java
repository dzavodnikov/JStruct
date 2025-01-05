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

import org.flatstruct.Field;
import org.flatstruct.Getter;
import org.flatstruct.Setter;

/**
 * Testing structure.
 */
public interface Point2D {

    @Field(int.class)
    String X_FIELD_NAME = "x";

    @Field(int.class)
    String Y_FIELD_NAME = "y";

    void setX(@Setter(X_FIELD_NAME) int x);

    @Getter(X_FIELD_NAME)
    int getX();

    void setY(@Setter(Y_FIELD_NAME) int y);

    @Getter(Y_FIELD_NAME)
    int getY();

    @Getter(value = X_FIELD_NAME, isSynchronized = true)
    int setXY(@Setter(value = X_FIELD_NAME, isSynchronized = true) int x,
            @Setter(value = Y_FIELD_NAME, isSynchronized = true) int y);

    @Getter(value = Y_FIELD_NAME, isSynchronized = true)
    int setYX(@Setter(value = Y_FIELD_NAME, isSynchronized = true) int y,
            @Setter(value = X_FIELD_NAME, isSynchronized = true) int x);
}
