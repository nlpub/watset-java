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

package org.nlpub.watset.util;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Useful implementations of {@link ContextSimilarity}.
 */
public final class ContextSimilarities {
    private ContextSimilarities() {
        throw new AssertionError();
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@link DummyContextSimilarity}.
     *
     * @param <V> the type of bag elements
     * @return an instance of {@link DummyContextSimilarity}
     */
    @SuppressWarnings("unused")
    public static <V> ContextSimilarity<V> dummy() {
        return new DummyContextSimilarity<>();
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@link CosineContextSimilarity}.
     *
     * @param <V> the type of bag elements
     * @return an instance of {@link CosineContextSimilarity}
     */
    public static <V> ContextSimilarity<V> cosine() {
        return new CosineContextSimilarity<>();
    }

    /**
     * A simple context similarity measure that always returns zero.
     *
     * @param <V> the type of bag elements
     */
    @SuppressWarnings("unused")
    public static class DummyContextSimilarity<V> implements ContextSimilarity<V> {
        @Override
        public Number apply(Map<V, Number> bag1, Map<V, Number> bag2) {
            return 0;
        }
    }

    /**
     * The classical cosine similarity measure for bags-of-words.
     *
     * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/queries-as-vectors-1.html">Queries as vectors</a>
     */
    public static class CosineContextSimilarity<V> implements ContextSimilarity<V> {
        @Override
        public Number apply(Map<V, Number> bag1, Map<V, Number> bag2) {
            final var union = new LinkedHashSet<>(bag1.keySet());
            union.addAll(bag2.keySet());

            final var vec1 = Vectors.transform(bag1, union);
            final var vec2 = Vectors.transform(bag2, union);

            final var norm1 = vec1.getNorm();
            final var norm2 = vec2.getNorm();

            if (norm1 == 0 || norm2 == 0) {
                return 0d;
            } else {
                return vec1.dotProduct(vec2) / (norm1 * norm2);
            }
        }
    }
}
