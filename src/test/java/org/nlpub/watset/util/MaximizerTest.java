/*
 * Copyright 2018 Dmitry Ustalov
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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.*;
import static org.nlpub.watset.util.Maximizer.*;
import static org.nlpub.watset.util.VectorsTest.bag1;

public class MaximizerTest {
    private final static Random random1 = new Random(1339);
    private final static Random random2 = new Random(1337);
    private final static Random random3 = new Random(1338);

    private static final Map<String, Number> bag4 = new HashMap<String, Number>() {{
        put("a", 1);
        put("b", 1);
        put("c", 1);
    }};

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg1 = argmax(bag1.entrySet().iterator(), e -> e.getValue().doubleValue());

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg2 = argmax(bag1.entrySet().iterator(), alwaysFalse(), e -> e.getValue().doubleValue());

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg3 = argmax(bag1.entrySet().iterator(), e -> e.getValue().doubleValue() < 0, e -> e.getValue().doubleValue());

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg4a = argmaxRandom(bag4.entrySet().iterator(), e -> e.getValue().doubleValue(), random1);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg4b = argmaxRandom(bag4.entrySet().iterator(), e -> e.getValue().doubleValue(), random2);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<Map.Entry<String, Number>> arg4c = argmaxRandom(bag4.entrySet().iterator(), e -> e.getValue().doubleValue(), random3);

    @Test
    public void testAlwaysTrue() {
        assertTrue(alwaysTrue().test(new Object()));
    }

    @Test
    public void testAlwaysFalse() {
        assertFalse(alwaysFalse().test(new Object()));
    }

    @Test
    public void testArgmax1() {
        assertTrue(arg1.isPresent());
        assertEquals(3, arg1.get().getValue());
    }

    @Test
    public void testArgmax2() {
        assertFalse(arg2.isPresent());
    }

    @Test
    public void testArgmax3() {
        assertTrue(arg3.isPresent());
        assertEquals(-5, arg3.get().getValue());
    }

    @Test
    public void testArgmax4() {
        assertTrue(arg4a.isPresent());
        assertEquals("a", arg4a.get().getKey());

        assertTrue(arg4b.isPresent());
        assertEquals("b", arg4b.get().getKey());

        assertTrue(arg4c.isPresent());
        assertEquals("c", arg4c.get().getKey());
    }
}
