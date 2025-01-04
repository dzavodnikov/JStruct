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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flatstruct.StructureFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Point2D} and {@link StructureFactory}.
 */
public class Point2DTest {

    @Test
    void testClassName() {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point = factory.create(Point2D.class);
        assertEquals("Structure_of_Point2D", point.getClass().getSimpleName());
    }

    @Test
    void testGetSet() {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point = factory.create(Point2D.class);

        assertEquals(0, point.getX());
        assertEquals(0, point.getY());

        point.setX(1);
        assertEquals(1, point.getX());
        assertEquals(0, point.getY());

        point.setX(2);
        assertEquals(2, point.getX());
        assertEquals(0, point.getY());

        point.setY(3);
        assertEquals(2, point.getX());
        assertEquals(3, point.getY());

        assertEquals(4, point.setXY(4, 5));
        assertEquals(4, point.getX());
        assertEquals(5, point.getY());

        assertEquals(7, point.setYX(7, 6));
        assertEquals(6, point.getX());
        assertEquals(7, point.getY());
    }

    @Test
    void testEquals() {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point1 = factory.create(Point2D.class);
        final Point2D point2 = factory.create(Point2D.class);
        assertFalse(point1.equals(null));
        assertTrue(point1.equals(point1));
        assertTrue(point1 != point2);
        assertTrue(point1.equals(point2));

        point1.setX(1);
        assertNotEquals(point1, point2);
        assertTrue(point1.equals(point1));

        point2.setX(1);
        assertEquals(point1, point2);

        assertFalse(point1.equals(""));
    }

    @Test
    void testHashCode() {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point1 = factory.create(Point2D.class);
        final Point2D point2 = factory.create(Point2D.class);
        assertEquals(point1.hashCode(), point2.hashCode());

        point1.setX(1);
        assertNotEquals(point1.hashCode(), point2.hashCode());

        point2.setX(1);
        assertEquals(point1.hashCode(), point2.hashCode());
    }

    @Test
    void testToString() {
        final StructureFactory<Point2D> factory = new StructureFactory<>();
        final Point2D point = factory.create(Point2D.class);

        assertEquals("org.flatstruct.example.structure.Structure_of_Point2D [\n    x=0\n    y=0\n]", point.toString());

        point.setXY(1, 2);
        assertEquals("org.flatstruct.example.structure.Structure_of_Point2D [\n    x=1\n    y=2\n]", point.toString());
    }
}
