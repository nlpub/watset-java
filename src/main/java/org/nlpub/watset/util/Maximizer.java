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

package org.nlpub.watset.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

/**
 * This is an utility class that implements a naïve approach for search a local maximum of a function.
 */
public interface Maximizer {
    /**
     * A predicate that is always true.
     *
     * @param <T> the type.
     * @return the absolute truth.
     */
    static <T> Predicate<T> alwaysTrue() {
        return (o) -> true;
    }

    /**
     * A predicate that is always false.
     *
     * @param <T> the type.
     * @return the absolute non-truth.
     */
    static <T> Predicate<T> alwaysFalse() {
        return (o) -> false;
    }

    /**
     * This is an utility method that finds an argument of the maxima for certain score function.
     *
     * @param it      finite iterator over the states.
     * @param checker the predicate that checks the suitability of an argument for the scoring.
     * @param scorer  the scoring function.
     * @param <V>     the argument type.
     * @param <S>     the score type.
     * @return non-empty optional that contains the first found argmax, otherwise an empty one.
     */
    static <V, S extends Comparable<S>> Optional<V> argmax(Iterator<V> it, Predicate<V> checker, Function<V, S> scorer) {
        V result = null;
        S score = null;

        while (it.hasNext()) {
            final V current = it.next();

            if (!checker.test(current)) continue;

            final S currentScore = scorer.apply(current);

            if (isNull(score) || (currentScore.compareTo(score) > 0)) {
                result = current;
                score = currentScore;
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * This is a simplified method that finds the argmax.
     *
     * @param it     finite iterator over the states.
     * @param scorer the scoring function.
     * @param <V>    the argument type.
     * @return non-empty optional that contains the first found argmax, otherwise an empty one.
     * @see Maximizer#argmax(Iterator, Predicate, Function)
     */
    static <V, S extends Comparable<S>> Optional<V> argmax(Iterator<V> it, Function<V, S> scorer) {
        return argmax(it, alwaysTrue(), scorer);
    }
}
