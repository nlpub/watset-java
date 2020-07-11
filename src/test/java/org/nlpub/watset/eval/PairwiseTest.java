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

package org.nlpub.watset.eval;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nlpub.watset.eval.Pairwise.transform;

public class PairwiseTest {
    static final Collection<Collection<String>> GOLD = Arrays.asList(
            List.of("bank", "riverbank", "streambank", "streamside"),
            List.of("bank", "building", "bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_1 = Arrays.asList(
            List.of("bank"),
            List.of("bank", "building"),
            List.of("riverbank", "streambank", "streamside"),
            List.of("bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_2 = Collections.singletonList(
            List.of("bank", "riverbank", "streambank", "streamside", "building", "bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_3 = Arrays.asList(
            List.of("bank"),
            List.of("building"),
            List.of("riverbank"),
            List.of("streambank"),
            List.of("streamside"),
            List.of("bank building")
    );

    private static final Set<String> ABC = Set.of("a", "b", "c");

    private static final Pairwise<String> pairwise = new Pairwise<>();

    @Test
    public void testGold() {
        final var result = pairwise.evaluate(GOLD, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1() {
        final var result = pairwise.evaluate(EXAMPLE_1, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(.44444, result.getRecall(), .0001);
        assertEquals(.61538, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2() {
        final var result = pairwise.evaluate(EXAMPLE_2, GOLD);
        assertEquals(.6, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.74999, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3() {
        final var result = pairwise.evaluate(EXAMPLE_3, GOLD);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(0, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }

    @Test
    public void testTransform() {
        final var pairs1 = transform(Collections.singleton(ABC));
        assertEquals(3, pairs1.size());
        assertTrue(pairs1.contains(Pairwise.pairOf("a", "b")));
        assertTrue(pairs1.contains(Pairwise.pairOf("b", "c")));
        assertTrue(pairs1.contains(Pairwise.pairOf("a", "c")));
    }
}
