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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;
import java.util.Set;

/**
 * This interface contains useful vector-space model transformations
 * from bag-of-words representations to real-valued vectors.
 */
public interface Vectors {
    /**
     * Transforms a bag-of-words into a real-valued vector.
     * The number of elements in the vector is equals to the size of the key set of the given bag.
     *
     * @param bag bag-of-words.
     * @param <V> bag element class.
     * @return real-valued vector.
     */
    static <V> RealVector transform(Map<V, Number> bag) {
        return transform(bag, bag.keySet());
    }

    /**
     * Transforms a bag-of-words into a real-valued vector.
     * The number of elements in the vector is equals to the size of the domain set.
     *
     * @param bag    bag-of-words.
     * @param domain set of dimensions.
     * @param <V>    bag element class.
     * @return real-valued vector.
     */
    static <V> RealVector transform(Map<V, Number> bag, Set<V> domain) {
        final double[] data = new double[domain.size()];

        int i = 0;

        for (final V key : domain) {
            data[i++] = bag.getOrDefault(key, 0).doubleValue();
        }

        return new ArrayRealVector(data, false);
    }
}
