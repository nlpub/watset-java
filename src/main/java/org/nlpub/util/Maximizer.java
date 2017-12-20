/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Maximizer {
    static <V> Optional<V> maximize(Iterator<V> it, Predicate<V> checker, Function<V, Double> scorer) {
        V result = null;
        double score = Double.NEGATIVE_INFINITY;

        while (it.hasNext()) {
            final V current = it.next();

            if (!checker.test(current)) continue;

            final double currentScore = scorer.apply(current);

            if (currentScore > score) {
                result = current;
                score = currentScore;
            }
        }

        return Optional.ofNullable(result);
    }
}
