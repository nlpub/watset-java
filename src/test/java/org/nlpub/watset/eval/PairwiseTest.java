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

import org.jgrapht.alg.util.Pair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nlpub.watset.eval.Pairwise.transform;

public class PairwiseTest {
    static final Collection<Collection<String>> GOLD = Arrays.asList(
            Arrays.asList("bank", "riverbank", "streambank", "streamside"),
            Arrays.asList("bank", "building", "bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_1 = Arrays.asList(
            Collections.singletonList("bank"),
            Arrays.asList("bank", "building"),
            Arrays.asList("riverbank", "streambank", "streamside"),
            Collections.singletonList("bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_2 = Collections.singletonList(
            Arrays.asList("bank", "riverbank", "streambank", "streamside", "building", "bank building")
    );

    static final Collection<Collection<String>> EXAMPLE_3 = Arrays.asList(
            Collections.singletonList("bank"),
            Collections.singletonList("building"),
            Collections.singletonList("riverbank"),
            Collections.singletonList("streambank"),
            Collections.singletonList("streamside"),
            Collections.singletonList("bank building")
    );

    private static final Set<String> ABC = new HashSet<>(Arrays.asList("a", "b", "c"));

    private static final Pairwise<String> pairwise = new Pairwise<>();

    @Test
    public void testGold() {
        final PrecisionRecall result = pairwise.evaluate(GOLD, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1() {
        final PrecisionRecall result = pairwise.evaluate(EXAMPLE_1, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(.44444, result.getRecall(), .0001);
        assertEquals(.61538, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2() {
        final PrecisionRecall result = pairwise.evaluate(EXAMPLE_2, GOLD);
        assertEquals(.6, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.74999, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3() {
        final PrecisionRecall result = pairwise.evaluate(EXAMPLE_3, GOLD);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(0, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }

    @Test
    public void testTransform() {
        final Set<Pair<String, String>> pairs1 = transform(Collections.singleton(ABC));
        assertEquals(3, pairs1.size());
        assertTrue(pairs1.contains(Pairwise.pairOf("a", "b")));
        assertTrue(pairs1.contains(Pairwise.pairOf("b", "c")));
        assertTrue(pairs1.contains(Pairwise.pairOf("a", "c")));
    }
}
