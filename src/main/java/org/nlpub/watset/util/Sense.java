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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.nlpub.watset.util.Maximizer.argmax;

/**
 * A monad that provides the wrapped value with a sense identifier.
 *
 * @param <V> the type of value
 */
public interface Sense<V> extends Supplier<V> {
    /**
     * Disambiguate each element of the context by maximizing its similarity to the senses in the inventory.
     * <p>
     * The method skips context keys marked as {@code ignored}.
     *
     * @param inventory  the sense inventory
     * @param similarity the similarity measure
     * @param context    the context to disambiguate
     * @param ignored    the ignored elements of {@code context}
     * @param <V>        the context element class
     * @return the disambiguated context
     */
    static <V> Map<Sense<V>, Number> disambiguate(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, ContextSimilarity<V> similarity, Map<V, Number> context, Collection<V> ignored) {
        final Map<Sense<V>, Number> dcontext = new HashMap<>(context.size());

        for (final var entry : context.entrySet()) {
            final var target = entry.getKey();

            if (ignored.contains(target)) continue;

            final var sense = argmax(inventory.getOrDefault(target, Collections.emptyMap()).keySet().iterator(), candidate -> {
                final var candidateContext = inventory.get(target).get(candidate);
                return similarity.apply(context, candidateContext).doubleValue();
            }).orElseThrow(() -> new IllegalArgumentException("Cannot find the sense for the word in context."));

            dcontext.put(sense, entry.getValue());
        }

        return dcontext;
    }
}