/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.vsm;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.nlpub.vsm.Vectors.*;

public class VectorsTest {
    private static final Map<String, Number> vec1 = new HashMap<String, Number>() {{
        put("a", 1);
        put("b", 3);
        put("c", -5);
    }};

    private static final Map<String, Number> vec2 = new HashMap<String, Number>() {{
        put("a", 4);
        put("b", -2);
        put("c", -1);
    }};

    private static final Map<String, Number> vec3 = new HashMap<String, Number>() {{
        put("b", 4);
        put("c", -2);
        put("d", -1);
    }};

    private static final Set<String> whitelist = new HashSet<String>() {{
        add("a");
        add("b");
        add("c");
        add("d");
    }};


    private static final Map<String, Number> vec1t = transform(vec1, whitelist);
    private static final Map<String, Number> vec3t = transform(vec3, whitelist);

    @Test
    public void testDot() {
        assertEquals(3., dot(vec1, vec2), 0);
    }

    @Test
    public void testNorm() {
        assertEquals(5.91, norm(vec1), .01);
        assertEquals(4.58, norm(vec2), .01);
        assertEquals(1, norm(Collections.singletonMap(1, 1)), 0);
    }

    @Test
    public void testTransform() {
        assertNotEquals(whitelist, vec1.keySet());
        assertEquals(whitelist, transform(vec1, whitelist).keySet());
    }

    @Test(expected = NullPointerException.class)
    public void testUntransformedDot() {
        dot(vec1, vec3);
    }

    @Test
    public void testTransformedDot() {
        assertEquals(22, dot(vec1t, vec3t), 0);
    }
}
