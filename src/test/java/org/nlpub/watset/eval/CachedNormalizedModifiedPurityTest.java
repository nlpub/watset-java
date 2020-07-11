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

package org.nlpub.watset.eval;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.nlpub.watset.eval.NormalizedModifiedPurityTest.*;

public class CachedNormalizedModifiedPurityTest {
    private static final NormalizedModifiedPurity<String> mpu = new CachedNormalizedModifiedPurity<>(false, true);
    private static final NormalizedModifiedPurity<String> pu = new NormalizedModifiedPurity<>(false, false);

    private static final NormalizedModifiedPurity<String> nmpu = new CachedNormalizedModifiedPurity<>();
    private static final NormalizedModifiedPurity<String> npu = new NormalizedModifiedPurity<>(true, false);

    @Test
    public void testGold() {
        final var result = NormalizedModifiedPurity.evaluate(mpu, pu, GOLD, GOLD);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testGoldNormalized() {
        final var result = NormalizedModifiedPurity.evaluate(nmpu, npu, GOLD_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(1, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(1, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1() {
        final var result = NormalizedModifiedPurity.evaluate(mpu, pu, EXAMPLE_1, GOLD);
        assertEquals(.71429, result.getPrecision(), .0001);
        assertEquals(.71429, result.getRecall(), .0001);
        assertEquals(.71429, result.getF1Score(), .0001);
    }

    @Test
    public void testExample1Normalized() {
        final var result = NormalizedModifiedPurity.evaluate(nmpu, npu, EXAMPLE_1_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(.75, result.getPrecision(), .0001);
        assertEquals(.75, result.getRecall(), .0001);
        assertEquals(.75, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2() {
        final var result = NormalizedModifiedPurity.evaluate(mpu, pu, EXAMPLE_2, GOLD);
        assertEquals(.66667, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.80000, result.getF1Score(), .0001);
    }

    @Test
    public void testExample2Normalized() {
        final var result = NormalizedModifiedPurity.evaluate(nmpu, npu, EXAMPLE_2_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(.66667, result.getPrecision(), .0001);
        assertEquals(1, result.getRecall(), .0001);
        assertEquals(.80000, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3() {
        final var result = NormalizedModifiedPurity.evaluate(mpu, pu, EXAMPLE_3, GOLD);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(.28571, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }

    @Test
    public void testExample3Normalized() {
        final var result = NormalizedModifiedPurity.evaluate(nmpu, npu, EXAMPLE_3_NORMALIZED, GOLD_NORMALIZED);
        assertEquals(0, result.getPrecision(), .0001);
        assertEquals(.33333, result.getRecall(), .0001);
        assertEquals(0, result.getF1Score(), .0001);
    }
}
