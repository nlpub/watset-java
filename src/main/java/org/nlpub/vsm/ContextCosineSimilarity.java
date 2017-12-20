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

package org.nlpub.vsm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContextCosineSimilarity<V> implements ContextSimilarity<V> {
    @Override
    public Number apply(Map<V, Number> bag1, Map<V, Number> bag2) {
        final Set<V> union = new HashSet<>(bag1.keySet());
        union.addAll(bag2.keySet());

        final Map<V, Number> vec1 = transform(bag1, union);
        final Map<V, Number> vec2 = transform(bag2, union);

        final double denominator = norm(vec1) * norm(vec2);

        if (denominator == 0.) return 0d;

        return dot(vec1, vec2) / denominator;
    }

    protected double dot(Map<V, Number> vec1, Map<V, Number> vec2) {
        return vec1.entrySet().stream().mapToDouble(e -> e.getValue().doubleValue() * vec2.get(e.getKey()).doubleValue()).sum();
    }

    protected double norm(Map<V, Number> bag) {
        return Math.sqrt(bag.values().stream().mapToDouble(weight -> Math.pow(weight.doubleValue(), 2)).sum());
    }

    protected Map<V, Number> transform(Map<V, Number> bag, Set<V> whitelist) {
        return whitelist.stream().collect(Collectors.toMap(Function.identity(), key -> bag.getOrDefault(key, 0.d)));
    }
}
