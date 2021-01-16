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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Utilities for statistical evaluation of computational experiments.
 */
public final class Sampling {
    private Sampling() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a bootstrapped dataset using sampling with replacement from the given dataset.
     * <p>
     * The dataset can be obtained using {@link Collection#toArray(Object[])}.
     *
     * @param dataset the dataset to sample from
     * @param random  the random number generator
     * @param <T>     the type of dataset elements
     * @return a sampled dataset
     */
    public static <T> List<T> sample(T[] dataset, Random random) {
        final var sample = new ArrayList<T>(dataset.length);

        for (var i = 0; i < dataset.length; i++) {
            sample.add(dataset[random.nextInt(dataset.length)]);
        }

        return sample;
    }
}
