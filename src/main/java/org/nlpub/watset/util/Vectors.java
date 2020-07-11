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

import java.util.Collection;
import java.util.Map;

/**
 * Utilities for mapping bags-of-words to real-valued vectors.
 *
 * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/dot-products-1.html">Dot products</a>
 */
public interface Vectors {
    /**
     * Transform the bag-of-words into a real-valued vector.
     * <p>
     * The number of elements in the vector is equal to the size of the key set of the given bag.
     *
     * @param bag the bag-of-words
     * @param <V> the type of bag elements
     * @return a real-valued vector
     */
    static <V> RealVector transform(Map<V, Number> bag) {
        return transform(bag, bag.keySet());
    }

    /**
     * Transform the bag-of-words into a real-valued vector.
     * <p>
     * The number of elements in the vector is equal to the size of the domain set.
     * <p>
     * Please make sure that all elements in {@code domain} are unique.
     *
     * @param bag    the bag-of-words
     * @param domain the collection of allowed elements of {@code bag}
     * @param <V>    the type of bag elements
     * @return a real-valued vector
     */
    static <V> RealVector transform(Map<V, Number> bag, Collection<V> domain) {
        final var data = new double[domain.size()];

        var i = 0;

        for (final var key : domain) {
            data[i++] = bag.getOrDefault(key, 0).doubleValue();
        }

        return new ArrayRealVector(data, false);
    }
}
