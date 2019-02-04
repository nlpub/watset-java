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

import org.nlpub.watset.util.Ranking;
import org.nlpub.watset.util.ContextSimilarity;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Watlink is a method for the generation and disambiguation of the upper-level elements for the clusters.
 *
 * @param <V> node class.
 * @see <a href="http://www.dialog-21.ru/media/3959/ustalovda.pdf">Ustalov (Dialogue 2017)</a>
 */
public class Watlink<V> {
    public static <V> Map<V, Map<Sense<V>, Map<V, Number>>> makeInventory(Collection<Collection<V>> clusters) {
        final Map<V, Map<Sense<V>, Map<V, Number>>> inventory = new HashMap<>();

        int i = 0;

        for (final Collection<V> cluster : clusters) {
            for (final V word : cluster) {
                if (!inventory.containsKey(word)) inventory.put(word, new HashMap<>());

                final Map<Sense<V>, Map<V, Number>> senses = inventory.get(word);
                final Sense<V> sense = new IndexedSense<>(word, i);

                final Map<V, Number> context = cluster.stream().
                        filter(element -> !Objects.equals(word, element)).
                        collect(toMap(identity(), weight -> 1));

                senses.put(sense, context);
            }

            i++;
        }

        return inventory;
    }

    private final Map<V, Map<Sense<V>, Map<V, Number>>> inventory;
    private final ContextSimilarity<V> similarity;
    private final Long k;

    public Watlink(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, ContextSimilarity<V> similarity, long k) {
        this.inventory = requireNonNull(inventory);
        this.similarity = requireNonNull(similarity);
        this.k = k;
    }

    public Map<Sense<V>, Number> retrieve(Collection<V> cluster, Map<V, Collection<V>> candidates) {
        final Map<V, Number> context = new HashMap<>();

        for (final V node : cluster) {
            if (!requireNonNull(candidates).containsKey(node)) continue;

            for (final V upper : candidates.get(node)) {
                context.put(upper, context.getOrDefault(upper, 0).doubleValue() + 1);
            }
        }

        /* TODO: weights should be cosine similarities, not counts! */
        final Map<Sense<V>, Number> dcontext = Sense.disambiguate(inventory, similarity, context, Collections.emptySet());

        return Ranking.getTopK(dcontext, k);
    }

    public void addMissingSenses(Map<V, Collection<V>> candidates) {
        final Set<V> missing = new HashSet<>();

        for (final Map.Entry<V, Collection<V>> entry : requireNonNull(candidates).entrySet()) {
            if (!inventory.containsKey(entry.getKey())) missing.add(entry.getKey());

            for (final V value : entry.getValue()) {
                if (!inventory.containsKey(value)) missing.add(value);
            }
        }

        for (final V word : missing) {
            inventory.put(word, Collections.singletonMap(new IndexedSense<>(word, 0), Collections.emptyMap()));
        }
    }
}
