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

package org.nlpub.eval;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NormalizedModifiedPurityTest {
    static Collection<Collection<String>> EXPECTED = Arrays.asList(
            Arrays.asList("bank", "riverbank", "streambank", "streamside"),
            Arrays.asList("bank", "building", "bank building")
    );

    static Collection<Collection<String>> ACTUAL_1 = Arrays.asList(
            Arrays.asList("bank"),
            Arrays.asList("bank", "building"),
            Arrays.asList("riverbank", "streambank", "streamside"),
            Arrays.asList("bank building")
    );

    static Collection<Collection<String>> ACTUAL_3 = Arrays.asList(
            Collections.singletonList("bank"),
            Collections.singletonList("building"),
            Collections.singletonList("riverbank"),
            Collections.singletonList("streambank"),
            Collections.singletonList("streamside"),
            Collections.singletonList("bank building")
    );

    @Test
    public void testEquivalence() {
        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(false, EXPECTED, EXPECTED);
        assertEquals(1, nmpu.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, nmpu.getNormalizedInversePurity(), 0.0001);
        assertEquals(1, nmpu.getFScore(), 0.0001);

        final NormalizedModifiedPurity<String> nmpuMulti = new NormalizedModifiedPurity<>(true, EXPECTED, EXPECTED);
        assertEquals(1, nmpuMulti.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, nmpuMulti.getNormalizedInversePurity(), 0.0001);
        assertEquals(1, nmpuMulti.getFScore(), 0.0001);
    }

    @Test
    public void testScores1() {
        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(false, EXPECTED, ACTUAL_1);
        assertEquals(0.714, nmpu.getNormalizedModifiedPurity(), 0.01);
        assertEquals(0.714, nmpu.getNormalizedInversePurity(), 0.01);
        assertEquals(0.714, nmpu.getFScore(), 0.01);

        final NormalizedModifiedPurity<String> nmpuMulti = new NormalizedModifiedPurity<>(true, EXPECTED, ACTUAL_1);
        assertEquals(0.75, nmpuMulti.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.75, nmpuMulti.getNormalizedInversePurity(), 0.0001);
        assertEquals(0.75, nmpuMulti.getFScore(), 0.0001);
    }

    @Test
    public void testScores3() {
        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(false, EXPECTED, ACTUAL_3);
        assertEquals(0, nmpu.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.28571, nmpu.getNormalizedInversePurity(), 0.0001);
        assertEquals(0, nmpu.getFScore(), 0.0001);

        final NormalizedModifiedPurity<String> nmpuMulti = new NormalizedModifiedPurity<>(true, EXPECTED, ACTUAL_3);
        assertEquals(0, nmpuMulti.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.33333, nmpuMulti.getNormalizedInversePurity(), 0.0001);
        assertEquals(0, nmpuMulti.getFScore(), 0.0001);
    }
}
