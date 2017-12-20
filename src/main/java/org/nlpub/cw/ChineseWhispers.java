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
import org.nlpub.cw.weighting.NodeSelector;
import org.nlpub.graph.Clustering;

import java.util.*;
import java.util.stream.Collectors;

public class ChineseWhispers<V, E> implements Clustering<V> {
    public static final Integer ITERATIONS = 20;

    protected final Graph<V, E> graph;
    protected final NodeSelector<V, E> nodeSelector;
    protected final Integer iterations;
    protected final Map<V, Integer> labels;

    public ChineseWhispers(Graph<V, E> graph, NodeSelector<V, E> nodeSelector, Integer iterations) {
        this.graph = graph;
        this.nodeSelector = nodeSelector;
        this.iterations = iterations;
        this.labels = new HashMap<>();
    }

    public ChineseWhispers(Graph<V, E> graph, NodeSelector<V, E> nodeSelector) {
        this(graph, nodeSelector, ITERATIONS);
    }

    @Override
    public void run() {
        final List<V> nodes = new ArrayList<>(graph.vertexSet());

        initializeLabels();

        for (int i = 0; i < this.iterations; i++) {
            boolean changed = false;

            Collections.shuffle(nodes);

            for (final V node : nodes) {
                final Optional<V> selection = this.nodeSelector.apply(this.graph, node);

                if (selection.isPresent()) {
                    final V neighbor = selection.get();

                    changed = changed || (!this.labels.get(node).equals(this.labels.get(neighbor)));

                    this.labels.put(node, this.labels.get(neighbor));
                }
            }

            if (!changed) break;
        }
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        final Map<Integer, List<Map.Entry<V, Integer>>> groups = this.labels.entrySet().stream().
                collect(Collectors.groupingBy(Map.Entry::getValue));

        final Set<Collection<V>> clusters = new HashSet<>();

        for (final List<Map.Entry<V, Integer>> cluster : groups.values()) {
            clusters.add(cluster.stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        }

        return clusters;
    }

    private void initializeLabels() {
        this.labels.clear();

        int i = 0;

        for (final V node : this.graph.vertexSet()) {
            this.labels.put(node, i++);
        }
    }
}
