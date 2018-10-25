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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ChineseWhispers<V, E> implements Clustering<V> {
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting, LabelSelector<V, E> selector) {
        return graph -> new ChineseWhispers<>(graph, weighting, selector);
    }

    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting, LabelSelector<V, E> selector, int iterations, Random random) {
        return graph -> new ChineseWhispers<>(graph, weighting, selector, iterations, random);
    }

    public static final int ITERATIONS = 20;

    private final Graph<V, E> graph;
    private final LabelSelector<V, E> selector;
    private final NodeWeighting<V, E> weighting;
    private final int iterations;
    private final Random random;
    private Map<V, Integer> labels;

    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting, LabelSelector<V, E> selector, int iterations, Random random) {
        this.graph = requireNonNull(graph);
        this.weighting = requireNonNull(weighting);
        this.selector = requireNonNull(selector);
        this.iterations = iterations;
        this.random = requireNonNull(random);
    }

    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting, LabelSelector<V, E> selector) {
        this(graph, weighting, selector, ITERATIONS, new Random());
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
                final int updated = selector.select(graph, labels, weighting, node);
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

        final Set<Collection<V>> clusters = new HashSet<>();

        for (final List<Map.Entry<V, Integer>> cluster : groups.values()) {
            clusters.add(cluster.stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        }

        return clusters;
    }
}
