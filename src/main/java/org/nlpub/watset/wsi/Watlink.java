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
import org.nlpub.watset.vsm.ContextSimilarity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Watlink is a method for the generation and disambiguation of the upper-level elements for the clusters.
 *
 * @see <a href="http://www.dialog-21.ru/media/3959/ustalovda.pdf">Ustalov et al. (Dialogue 2017)</a>
 */
public class Watlink<V> {
    private final Map<V, Map<Sense<V>, Map<V, Number>>> inventory;
    private final ContextSimilarity<V> similarity;
    private final Long k;

    public Watlink(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, ContextSimilarity<V> similarity, long k) {
        this.inventory = inventory;
        this.similarity = similarity;
        this.k = k;
    }

    public Map<Sense<V>, Number> retrieve(Collection<V> cluster, Map<V, Collection<V>> candidates) {
        final Map<V, Number> context = new HashMap<>();

        for (final V node : cluster) {
            if (!candidates.containsKey(node)) continue;

            for (final V upper : candidates.get(node)) {
                context.put(upper, context.getOrDefault(upper, 0).doubleValue() + 1);
            }
        }

        final Map<Sense<V>, Number> dcontext = Sense.disambiguate(inventory, similarity, context, Collections.emptySet());

        return Ranking.getTopK(dcontext, k);
    }
}
