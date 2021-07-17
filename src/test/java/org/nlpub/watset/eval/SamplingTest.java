/*
 * Copyright 2020 Dmitry Ustalov
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


import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SamplingTest {
    static final Integer[] EMPTY_ARRAY = {};
    static final List<Integer> EMPTY_LIST = Collections.emptyList();

    static final Integer[] ONE_ARRAY = {1};
    static final List<Integer> ONE_LIST = List.of(1);

    static final Random random = new Random();

    @Test
    public void testSample0() {
        assertEquals(EMPTY_LIST, Sampling.sample(EMPTY_ARRAY, random));
    }

    @Test
    public void testSample1() {
        assertEquals(ONE_LIST, Sampling.sample(ONE_ARRAY, random));
    }
}
