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

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.nlpub.watset.eval.NormalizedModifiedPurity.transform;

public class NormalizedModifiedPurityTest {
    private static final Collection<Map<String, Double>> EXPECTED = transform(PairwiseTest.EXPECTED);

    private static final Collection<Map<String, Double>> ACTUAL_1 = transform(PairwiseTest.ACTUAL_1);

    private static final Collection<Map<String, Double>> ACTUAL_2 = transform(PairwiseTest.ACTUAL_2);

    private static final Collection<Map<String, Double>> ACTUAL_3 = transform(PairwiseTest.ACTUAL_3);

    @Test
    public void testEquivalence() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(EXPECTED, EXPECTED, false);
        final PrecisionRecall mpuResult = mpu.get();
        assertEquals(1, mpuResult.getPrecision(), .0001);
        assertEquals(1, mpuResult.getRecall(), .0001);
        assertEquals(1, mpuResult.getF1Score(), .0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(EXPECTED, EXPECTED);
        final PrecisionRecall nmpuResult = nmpu.get();
        assertEquals(1, nmpuResult.getPrecision(), .0001);
        assertEquals(1, nmpuResult.getRecall(), .0001);
        assertEquals(1, nmpuResult.getF1Score(), .0001);
    }

    @Test
    public void testScores1() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_1, EXPECTED, false);
        final PrecisionRecall mpuResult = mpu.get();
        assertEquals(.71429, mpuResult.getPrecision(), .0001);
        assertEquals(.71429, mpuResult.getRecall(), .0001);
        assertEquals(.71429, mpuResult.getF1Score(), .0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_1, EXPECTED);
        final PrecisionRecall nmpuResult = nmpu.get();
        assertEquals(.75, nmpuResult.getPrecision(), .0001);
        assertEquals(.75, nmpuResult.getRecall(), .0001);
        assertEquals(.75, nmpuResult.getF1Score(), .0001);
    }

    @Test
    public void testScores2() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_2, EXPECTED, false);
        final PrecisionRecall mpuResult = mpu.get();
        assertEquals(.66667, mpuResult.getPrecision(), .0001);
        assertEquals(1, mpuResult.getRecall(), .0001);
        assertEquals(.80000, mpuResult.getF1Score(), .0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_2, EXPECTED);
        final PrecisionRecall nmpuResult = nmpu.get();
        assertEquals(.66667, nmpuResult.getPrecision(), .0001);
        assertEquals(1, nmpuResult.getRecall(), .0001);
        assertEquals(.80000, nmpuResult.getF1Score(), .0001);
    }

    @Test
    public void testScores3() {
        final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(ACTUAL_3, EXPECTED, false);
        final PrecisionRecall mpuResult = mpu.get();
        assertEquals(0, mpuResult.getPrecision(), .0001);
        assertEquals(.28571, mpuResult.getRecall(), .0001);
        assertEquals(0, mpuResult.getF1Score(), .0001);

        final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>(ACTUAL_3, EXPECTED);
        final PrecisionRecall nmpuResult = nmpu.get();
        assertEquals(0, nmpuResult.getPrecision(), .0001);
        assertEquals(.33333, nmpuResult.getRecall(), .0001);
        assertEquals(0, nmpuResult.getF1Score(), .0001);
    }
}
