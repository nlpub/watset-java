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

package org.nlpub.watset;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.nlpub.graph.Clustering;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SenseInduction<V, E> implements Runnable {
    protected final Graph<V, E> graph;
    protected final V target;
    protected final Function<Graph<V, E>, Clustering<V>> clusteringProvider;
    protected final int radius;
    protected final Collection<Collection<V>> clusters;

    public SenseInduction(Graph<V, E> graph, V target, Function<Graph<V, E>, Clustering<V>> clusteringProvider, int radius) {
        this.graph = graph;
        this.target = target;
        this.clusteringProvider = clusteringProvider;
        this.radius = radius;
        this.clusters = new HashSet<>();
    }

    @Override
    public void run() {
        clusters.clear();

        final Set<V> neighborhood = new HashSet<>();

        final ClosestFirstIterator<V, E> it = new ClosestFirstIterator<>(graph, target, radius);
        it.forEachRemaining(neighborhood::add);
        neighborhood.remove(this.target);

        final Graph<V, E> ego = new AsSubgraph<>(graph, neighborhood);

        final Clustering<V> clustering = clusteringProvider.apply(ego);
        clustering.run();

        clusters.addAll(clustering.getClusters());
    }

    public Map<Integer, Map<V, Number>> getSenses() {
        int i = 0;

        final Map<Integer, Map<V, Number>> senses = new HashMap<>();

        for (Collection<V> cluster : clusters) {
            final Map<V, Number> bag = cluster.stream().collect(Collectors.toMap(
                    Function.identity(),
                    neighbor -> graph.getEdgeWeight(graph.getEdge(this.target, neighbor))
            ));

            senses.put(i++, bag);
        }

        return senses;
    }
}
