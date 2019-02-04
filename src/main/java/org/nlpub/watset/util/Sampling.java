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

package org.nlpub.watset.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * This interface includes utility functions which are useful in
 * statistical evaluation of the results.
 */
public interface Sampling {
    /**
     * Creates a bootstrapped dataset using sampling with replacement
     * from the given dataset. The dataset can be obtained using
     * {@link Collection#toArray(Object[])}.
     *
     * @param dataset a dataset to sample from.
     * @param random  a random number generator.
     * @param <T>     an element class.
     * @return a sampled dataset.
     */
    static <T> Collection<T> sample(T[] dataset, Random random) {
        final List<T> sample = new ArrayList<>(dataset.length);

        for (int i = 0; i < dataset.length; i++) {
            sample.add(dataset[random.nextInt(dataset.length)]);
        }

        return sample;
    }
}
