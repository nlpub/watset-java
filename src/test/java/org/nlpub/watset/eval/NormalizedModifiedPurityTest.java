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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NormalizedModifiedPurityTest {
    private static final Collection<Map<String, Double>> EXPECTED = NormalizedModifiedPurity.transform(Arrays.asList(
            Arrays.asList("bank", "riverbank", "streambank", "streamside"),
            Arrays.asList("bank", "building", "bank building")
    ));

    private static final Collection<Map<String, Double>> ACTUAL_1 = NormalizedModifiedPurity.transform(Arrays.asList(
            Collections.singletonList("bank"),
            Arrays.asList("bank", "building"),
            Arrays.asList("riverbank", "streambank", "streamside"),
            Collections.singletonList("bank building")
    ));

    private static final Collection<Map<String, Double>> ACTUAL_2 = NormalizedModifiedPurity.transform(Collections.singletonList(
            Arrays.asList("bank", "riverbank", "streambank", "streamside", "building", "bank building")
    ));

    private static final Collection<Map<String, Double>> ACTUAL_3 = NormalizedModifiedPurity.transform(Arrays.asList(
            Collections.singletonList("bank"),
            Collections.singletonList("building"),
            Collections.singletonList("riverbank"),
            Collections.singletonList("streambank"),
            Collections.singletonList("streamside"),
            Collections.singletonList("bank building")
    ));

    @Test
    public void testEquivalence() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(EXPECTED, EXPECTED, false);
        final NormalizedModifiedPurity.Result mpuResult = mpu.get();
        assertEquals(1, mpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, mpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(1, mpuResult.getF1Score(), 0.0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(EXPECTED, EXPECTED);
        final NormalizedModifiedPurity.Result nmpuResult = nmpu.get();
        assertEquals(1, nmpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, nmpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(1, nmpuResult.getF1Score(), 0.0001);
    }

    @Test
    public void testScores1() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_1, EXPECTED, false);
        final NormalizedModifiedPurity.Result mpuResult = mpu.get();
        assertEquals(0.71429, mpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.71429, mpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0.71429, mpuResult.getF1Score(), 0.01);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_1, EXPECTED);
        final NormalizedModifiedPurity.Result nmpuResult = nmpu.get();
        assertEquals(0.75, nmpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.75, nmpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0.75, nmpuResult.getF1Score(), 0.0001);
    }

    @Test
    public void testScores2() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_2, EXPECTED, false);
        final NormalizedModifiedPurity.Result mpuResult = mpu.get();
        assertEquals(0.66667, mpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, mpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0.80000, mpuResult.getF1Score(), 0.0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_2, EXPECTED);
        final NormalizedModifiedPurity.Result nmpuResult = nmpu.get();
        assertEquals(0.66667, nmpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(1, nmpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0.80000, nmpuResult.getF1Score(), 0.0001);
    }

    @Test
    public void testScores3() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_3, EXPECTED, false);
        final NormalizedModifiedPurity.Result mpuResult = mpu.get();
        assertEquals(0, mpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.28571, mpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0, mpuResult.getF1Score(), 0.0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_3, EXPECTED);
        final NormalizedModifiedPurity.Result nmpuResult = nmpu.get();
        assertEquals(0, nmpuResult.getNormalizedModifiedPurity(), 0.0001);
        assertEquals(0.33333, nmpuResult.getNormalizedInversePurity(), 0.0001);
        assertEquals(0, nmpuResult.getF1Score(), 0.0001);
    }
}
