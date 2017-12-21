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
import org.nlpub.graph.Clustering;
import org.nlpub.util.Neighbors;
import org.nlpub.watset.sense.IndexedSense;
import org.nlpub.watset.sense.Sense;

import java.util.*;
import java.util.function.Function;

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

        final Set<V> neighborhood = Neighbors.neighborSetOf(graph, target);

        final Graph<V, E> ego = new AsSubgraph<>(graph, new HashSet<>(neighborhood));

        final Clustering<V> clustering = clusteringProvider.apply(ego);
        clustering.run();

        clusters.addAll(clustering.getClusters());
    }

    public Map<Sense<V>, Map<V, Number>> getSenses() {
        int i = 0;

        final Map<Sense<V>, Map<V, Number>> senses = new HashMap<>();

        for (final Collection<V> cluster : clusters) {
            final Map<V, Number> context = new HashMap<>();

            for (final V neighbor : cluster) {
                context.put(neighbor, this.graph.getEdgeWeight(this.graph.getEdge(this.target, neighbor)));
            }

            senses.put(new IndexedSense<>(this.target, i++), context);
        }

        return senses;
    }
}
