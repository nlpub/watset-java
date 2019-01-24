/*
 * Copyright 2019 Dmitry Ustalov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.nlpub.watset.util;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class VectorsTest {
    public static final Map<String, Number> bag1 = new HashMap<String, Number>() {{
        put("a", 1);
        put("b", 3);
        put("c", -5);
    }};

    private static final Map<String, Number> bag2 = new HashMap<String, Number>() {{
        put("a", 4);
        put("b", -2);
        put("c", -1);
    }};

    private static final Map<String, Number> bag3 = new HashMap<String, Number>() {{
        put("b", 4);
        put("c", -2);
        put("d", -1);
    }};

    private static final Set<String> whitelist = new LinkedHashSet<String>() {{
        add("a");
        add("b");
        add("c");
        add("d");
    }};

    private static final RealVector vec1 = Vectors.transform(bag1);
    private static final RealVector vec2 = Vectors.transform(bag2);
    private static final RealVector vec3 = Vectors.transform(bag3);

    private static final RealVector vec1t4 = Vectors.transform(bag1, whitelist);
    private static final RealVector vec2t4 = Vectors.transform(bag2, whitelist);
    private static final RealVector vec3t4 = Vectors.transform(bag3, whitelist);

    @Test
    public void testDot() {
        assertEquals(3., vec1.dotProduct(vec2), 0);
    }

    @Test
    public void testNorm() {
        assertEquals(5.91, vec1.getNorm(), .01);
        assertEquals(4.58, vec2.getNorm(), .01);
        assertEquals(4.58, vec3.getNorm(), .01);
        assertEquals(1, Vectors.transform(Collections.singletonMap(1, 1)).getNorm(), 0);
    }

    @Test
    public void testTransform() {
        assertNotEquals(whitelist, bag1.keySet());
        assertNotEquals(whitelist, bag2.keySet());
        assertNotEquals(whitelist, bag3.keySet());
        assertArrayEquals(new double[]{1, 3, -5, 0}, vec1t4.toArray(), 0);
        assertArrayEquals(new double[]{4, -2, -1, 0}, vec2t4.toArray(), 0);
        assertArrayEquals(new double[]{0, 4, -2, -1}, vec3t4.toArray(), 0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testUntransformedDot() {
        vec1.dotProduct(vec1t4);
    }

    @Test
    public void testTransformedDot() {
        assertEquals(22, vec1t4.dotProduct(vec3t4), 0);
    }
}
