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

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

/**
 * Utilities for searching arguments of the maxima of the function.
 */
public final class Maximizer {
    private Maximizer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * A predicate that is always true.
     *
     * @param <T> the type
     * @return the absolute truth
     */
    public static <T> Predicate<T> alwaysTrue() {
        return (o) -> true;
    }

    /**
     * A predicate that is always false.
     *
     * @param <T> the type
     * @return the absolute non-truth
     */
    public static <T> Predicate<T> alwaysFalse() {
        return (o) -> false;
    }

    /**
     * Find the first argument of the maximum (argmax) of the function.
     *
     * @param iterable the finite iterator
     * @param checker  the predicate that evaluates the suitability of the argument
     * @param scorer   the scoring function
     * @param <V>      the argument type
     * @param <S>      the score type
     * @return a non-empty optional that contains the first found argmax, otherwise the empty one
     */
    public static <V, S extends Comparable<S>> Optional<V> argmax(Iterable<V> iterable, Predicate<V> checker, Function<V, S> scorer) {
        V result = null;
        S score = null;

        for (final var current : iterable) {
            if (!checker.test(current)) continue;

            final var currentScore = scorer.apply(current);

            if (isNull(score) || (currentScore.compareTo(score) > 0)) {
                result = current;
                score = currentScore;
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * Find the first argument of the maximum (argmax) of the function.
     *
     * @param iterable the finite iterator
     * @param scorer   the scoring function
     * @param <V>      the argument type
     * @param <S>      the score type
     * @return a non-empty optional that contains the first found argmax, otherwise the empty one
     * @see #argmax(Iterable, Predicate, Function)
     */
    public static <V, S extends Comparable<S>> Optional<V> argmax(Iterable<V> iterable, Function<V, S> scorer) {
        return argmax(iterable, alwaysTrue(), scorer);
    }

    /**
     * Find the arguments of the maxima (argmax) of the function and randomly choose any of them.
     *
     * @param iterable the finite iterator
     * @param scorer   the scoring function
     * @param random   the random number generator
     * @param <V>      the argument type
     * @param <S>      the score type
     * @return a non-empty optional that contains the randomly chosen argmax, otherwise the empty one
     */
    public static <V, S extends Comparable<S>> Optional<V> argrandmax(Iterable<V> iterable, Function<V, S> scorer, Random random) {
        final var results = new ArrayList<V>();
        S score = null;

        for (final var current : iterable) {
            final var currentScore = scorer.apply(current);

            final var compare = isNull(score) ? 1 : currentScore.compareTo(score);

            if (compare > 0) {
                results.clear();
                score = currentScore;
            }

            if (compare >= 0) {
                results.add(current);
            }
        }

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(random.nextInt(results.size())));
    }
}
