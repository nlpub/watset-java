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
import static org.nlpub.watset.eval.NormalizedModifiedPurity.normalize;
import static org.nlpub.watset.eval.NormalizedModifiedPurity.transform;

public class NormalizedModifiedPurityTest {
    static final Collection<Map<String, Double>> GOLD = transform(PairwiseTest.GOLD);
    static final Collection<Map<String, Double>> GOLD_NORMALIZED = normalize(GOLD);

    static final Collection<Map<String, Double>> EXAMPLE_1 = transform(PairwiseTest.EXAMPLE_1);
    static final Collection<Map<String, Double>> EXAMPLE_1_NORMALIZED = normalize(EXAMPLE_1);

    static final Collection<Map<String, Double>> EXAMPLE_2 = transform(PairwiseTest.EXAMPLE_2);
    static final Collection<Map<String, Double>> EXAMPLE_2_NORMALIZED = normalize(EXAMPLE_2);

    static final Collection<Map<String, Double>> EXAMPLE_3 = transform(PairwiseTest.EXAMPLE_3);
    static final Collection<Map<String, Double>> EXAMPLE_3_NORMALIZED = normalize(EXAMPLE_3);

    private static final NormalizedModifiedPurity<String> mpu = new NormalizedModifiedPurity<>(false, true);

    private static final NormalizedModifiedPurity<String> nmpu = new NormalizedModifiedPurity<>();

    @Test
    public void testGold() {
        final PrecisionRecall result = mpu.evaluate(GOLD, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testGoldNormalized() {
        final PrecisionRecall result = nmpu.evaluate(GOLD_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1() {
        final PrecisionRecall result = mpu.evaluate(EXAMPLE_1, GOLD);
        assertEquals(.71429, result.getPrecision(), .0001);
        assertEquals(.71429, result.getRecall(), .0001);
        assertEquals(.71429, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1Normalized() {
        final PrecisionRecall result = nmpu.evaluate(EXAMPLE_1_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(.75, result.getPrecision(), .0001);
        assertEquals(.75, result.getRecall(), .0001);
        assertEquals(.75, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2() {
        final PrecisionRecall result = mpu.evaluate(EXAMPLE_2, GOLD);
        assertEquals(.66667, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.80000, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2Normalized() {
        final PrecisionRecall result = nmpu.evaluate(EXAMPLE_2_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(.66667, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.80000, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3() {
        final PrecisionRecall result = mpu.evaluate(EXAMPLE_3, GOLD);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(.28571, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3Normalized() {
        final PrecisionRecall result = nmpu.evaluate(EXAMPLE_3_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(.33333, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }
}
