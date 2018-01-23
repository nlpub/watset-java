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

package org.nlpub.cw;

import org.jgrapht.Graph;
import org.nlpub.graph.Clustering;
import org.nlpub.util.Neighbors;

import java.util.*;
import java.util.stream.Collectors;

import static org.nlpub.util.Maximizer.argmax;

public class ChineseWhispers<V, E> implements Clustering<V> {
    public static final Integer ITERATIONS = 20;

    private final Graph<V, E> graph;
    private final NodeWeighting<V, E> weighting;
    private final Integer iterations;
    private final Random random;
    private Map<V, Integer> labels;

    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting, Integer iterations, Random random) {
        this.graph = graph;
        this.weighting = weighting;
        this.iterations = iterations;
        this.random = random;
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
                final int updated = choose(graph, labels, weighting, node);
                final int previous = labels.put(node, updated);
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

    private Integer choose(Graph<V, E> graph, Map<V, Integer> labels, NodeWeighting<V, E> weighting, V node) {
        final Map<Integer, Double> weights = new HashMap<>();

        final Iterator<V> neighbors = Neighbors.neighborIterator(graph, node);

        neighbors.forEachRemaining(neighbor -> {
            final int label = labels.get(neighbor);
            weights.put(label, weights.getOrDefault(label, 0d) + weighting.apply(graph, node, neighbor));
        });

        final Optional<Map.Entry<Integer, Double>> label = argmax(weights.entrySet().iterator(), Map.Entry::getValue);

        return label.map(Map.Entry::getKey).orElseGet(() -> labels.get(node));
    }
}
