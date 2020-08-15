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

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.nlpub.watset.util.Maximizer.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MaximizerTest {
    private final static Random random = new Random(1337);

    private final static int SAMPLES = 10000;

    private static final Map<String, Number> bag1 = Map.of("a", 1, "b", 3, "c", -5);
    private static final Map<String, Number> bag2 = Map.of("a", 1, "b", 1, "c", 1);

    final Optional<Map.Entry<String, Number>> arg1 = argmax(bag1.entrySet(), e -> e.getValue().doubleValue());
    final Optional<Map.Entry<String, Number>> arg2 = argmax(bag1.entrySet(), alwaysFalse(), e -> e.getValue().doubleValue());
    final Optional<Map.Entry<String, Number>> arg3 = argmax(bag1.entrySet(), e -> e.getValue().doubleValue() < 0, e -> e.getValue().doubleValue());

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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void testArgmaxRandom() {
        final var samples = IntStream.range(0, SAMPLES).
                mapToObj(i -> argrandmax(bag2.entrySet(), e -> e.getValue().doubleValue(), random).get().getKey()).
                collect(Collectors.toSet());

        assertEquals(bag2.keySet(), samples);
    }
}
