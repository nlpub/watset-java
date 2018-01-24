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

package org.nlpub.watset.wsi;

import org.nlpub.watset.vsm.ContextSimilarity;

import java.util.*;
import java.util.function.Supplier;

import static org.nlpub.watset.util.Maximizer.argmax;

public interface Sense<V> extends Supplier<V> {
    static <V> Map<Sense<V>, Number> disambiguate(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, ContextSimilarity<V> similarity, Map<V, Number> context, Set<V> ignored) {
        final Map<Sense<V>, Number> dcontext = new HashMap<>();

        for (final Map.Entry<V, Number> entry : context.entrySet()) {
            final V target = entry.getKey();

            if (ignored.contains(target)) continue;

            final Optional<Sense<V>> result = argmax(inventory.getOrDefault(target, Collections.emptyMap()).keySet().iterator(), candidate -> {
                final Map<V, Number> candidateContext = inventory.get(target).get(candidate);
                return similarity.apply(context, candidateContext).doubleValue();
            });

            if (result.isPresent()) {
                dcontext.put(result.get(), entry.getValue());
            } else {
                throw new IllegalArgumentException("Cannot find the sense for the word in context.");
            }
        }

        return dcontext;
    }
}
