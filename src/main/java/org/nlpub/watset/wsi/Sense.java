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

import org.nlpub.watset.util.ContextSimilarity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.nlpub.watset.util.Maximizer.argmax;

/**
 * A sense is a simple wrapper over an object that has a different address in the memory.
 *
 * @param <V> wrapped class.
 */
public interface Sense<V> extends Supplier<V> {
    /**
     * Disambiguate a context as according to its similarity to the senses in the inventory.
     * The method skips context keys which are marked as ignored.
     *
     * @param inventory  sense inventory.
     * @param similarity similarity measure.
     * @param context    context to disambiguate.
     * @param ignored    blacklisted keys.
     * @param <V>        context element class.
     * @return disambiguated context.
     */
    static <V> Map<Sense<V>, Number> disambiguate(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, ContextSimilarity<V> similarity, Map<V, Number> context, Set<V> ignored) {
        final Map<Sense<V>, Number> dcontext = new HashMap<>(context.size());

        for (final Map.Entry<V, Number> entry : context.entrySet()) {
            final V target = entry.getKey();

            if (ignored.contains(target)) continue;

            final Sense<V> sense = argmax(inventory.getOrDefault(target, Collections.emptyMap()).keySet().iterator(), candidate -> {
                final Map<V, Number> candidateContext = inventory.get(target).get(candidate);
                return similarity.apply(context, candidateContext).doubleValue();
            }).orElseThrow(() -> new IllegalArgumentException("Cannot find the sense for the word in context."));

            dcontext.put(sense, entry.getValue());
        }

        return dcontext;
    }
}
