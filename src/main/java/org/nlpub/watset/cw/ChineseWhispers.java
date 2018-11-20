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
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.Neighbors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.nlpub.watset.util.Maximizer.argmax;

public class ChineseWhispers<V, E> implements Clustering<V> {
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting) {
        return graph -> new ChineseWhispers<>(graph, weighting);
    }

    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting, int iterations, Random random) {
        return graph -> new ChineseWhispers<>(graph, weighting, iterations, random);
    }

    public static final int ITERATIONS = 20;

    protected final Graph<V, E> graph;
    protected final NodeWeighting<V, E> weighting;
    protected final int iterations;
    protected final Random random;
    protected Map<V, Integer> labels;

    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting, int iterations, Random random) {
        this.graph = requireNonNull(graph);
        this.weighting = requireNonNull(weighting);
        this.iterations = iterations;
        this.random = requireNonNull(random);
    }

    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting) {
        this(graph, weighting, ITERATIONS, new Random());
    }

    @Override
    public void run() {
        final List<V> nodes = new ArrayList<>(graph.vertexSet());

        labels = new HashMap<>(nodes.size());

        int i = 0;

        for (final V node : graph.vertexSet()) {
            labels.put(node, i++);
        }

        for (i = 0; i < iterations; i++) {
            boolean changed = false;

            Collections.shuffle(nodes, random);

            for (final V node : nodes) {
                final Map<Integer, Double> scores = score(graph, labels, weighting, node);

                final Optional<Map.Entry<Integer, Double>> label = argmax(scores.entrySet().iterator(), Map.Entry::getValue);

                final int updated = label.isPresent() ? label.get().getKey() : labels.get(node);

                // labels.put() never returns null for a known node
                @SuppressWarnings("ConstantConditions") final int previous = labels.put(node, updated);
                changed = changed || (previous != updated);
            }

            if (!changed) break;
        }
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        final Map<Integer, List<Map.Entry<V, Integer>>> groups = labels.entrySet().stream().
                collect(Collectors.groupingBy(Map.Entry::getValue));

        final List<Collection<V>> clusters = new ArrayList<>(groups.size());

        for (final List<Map.Entry<V, Integer>> cluster : groups.values()) {
            clusters.add(cluster.stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        }

        return clusters;
    }

    /**
     * This label selector selects the label class having the maximal total weight in the neighborhood.
     *
     * @param graph     the graph.
     * @param labels    node labels.
     * @param weighting edge weighting.
     * @param node      the target node.
     * @return label-weight map.
     */
    protected Map<Integer, Double> score(Graph<V, E> graph, Map<V, Integer> labels, NodeWeighting<V, E> weighting, V node) {
        final Map<Integer, Double> weights = new HashMap<>();

        final Iterator<V> neighbors = Neighbors.neighborIterator(graph, node);

        neighbors.forEachRemaining(neighbor -> {
            final int label = labels.get(neighbor);
            weights.merge(label, weighting.apply(graph, labels, node, neighbor), Double::sum);
        });

        return weights;
    }
}
