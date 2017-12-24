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

import static org.nlpub.vsm.Vectors.*;

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
}
