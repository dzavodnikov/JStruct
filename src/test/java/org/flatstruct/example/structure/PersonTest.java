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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.flatstruct.StructureFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Person} and {@link StructureFactory}.
 */
public class PersonTest {

    @Test
    void testClassName() {
        final StructureFactory<Person> factory = new StructureFactory<>();
        final Person person = factory.create(Person.class);
        assertEquals("Structure_of_Person", person.getClass().getSimpleName());
    }

    @Test
    void testGetSet() {
        final StructureFactory<Person> factory = new StructureFactory<>();
        final Person person = factory.create(Person.class);

        assertNull(person.getName());
        assertEquals(0, person.getAge());

        person.setName("John Doe");
        assertEquals("John Doe", person.getName());
        assertEquals(0, person.getAge());

        person.setName("Jane Doe");
        assertEquals("Jane Doe", person.getName());
        assertEquals(0, person.getAge());

        person.setAge(25);
        assertEquals("Jane Doe", person.getName());
        assertEquals(25, person.getAge());
    }
}
