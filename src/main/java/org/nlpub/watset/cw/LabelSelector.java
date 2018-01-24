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

package org.nlpub.watset.cw;

import org.jgrapht.Graph;
import org.nlpub.watset.util.Neighbors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.nlpub.watset.util.Maximizer.argmax;

@FunctionalInterface
public interface LabelSelector<V, E> {
    int select(Graph<V, E> graph, Map<V, Integer> labels, NodeWeighting<V, E> weighting, V node);

    /**
     * This is a simple label selector that select only the node having the maximal weight.
     *
     * @param <V> vertices.
     * @param <E> edges.
     * @return the label.
     */
    static <V, E> LabelSelector<V, E> single() {
        return (graph, labels, weighting, node) -> {
            final Iterator<V> neighbors = Neighbors.neighborIterator(graph, node);

            final Optional<V> match = argmax(neighbors, neighbor -> weighting.apply(graph, labels, node, neighbor));

            return match.map(labels::get).orElseGet(() -> labels.get(node));
        };
    }

    /**
     * This label selector selects the label class having the maximal total weight in the neighborhood.
     *
     * @param <V> vertices.
     * @param <E> edges.
     * @return the label.
     */
    static <V, E> LabelSelector<V, E> total() {
        return (graph, labels, weighting, node) -> {
            final Map<Integer, Double> weights = new HashMap<>();

            final Iterator<V> neighbors = Neighbors.neighborIterator(graph, node);

            neighbors.forEachRemaining(neighbor -> {
                final int label = labels.get(neighbor);
                weights.put(label, weights.getOrDefault(label, 0d) + weighting.apply(graph, labels, node, neighbor));
            });

            final Optional<Map.Entry<Integer, Double>> label = argmax(weights.entrySet().iterator(), Map.Entry::getValue);

            return label.map(Map.Entry::getKey).orElseGet(() -> labels.get(node));
        };
    }
}
