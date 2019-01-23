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

import org.jgrapht.Graph;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.Neighbors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A simple graph-based word sense induction approach. It clusters node neighborhoods.
 *
 * @param <V> node class.
 * @param <E> edge class.
 */
public class SenseInduction<V, E> implements Runnable {
    private final Graph<V, E> graph;
    private final V target;
    private final Function<Graph<V, E>, Clustering<V>> clusteringProvider;
    private Collection<Collection<V>> clusters;

    public SenseInduction(Graph<V, E> graph, V target, Function<Graph<V, E>, Clustering<V>> clusteringProvider) {
        this.graph = requireNonNull(graph);
        this.target = requireNonNull(target);
        this.clusteringProvider = requireNonNull(clusteringProvider);
    }

    @Override
    public void run() {
        clusters = null;

        final Graph<V, E> ego = Neighbors.neighborhoodGraph(graph, target);

        final Clustering<V> clustering = clusteringProvider.apply(ego);
        clustering.run();

        clusters = clustering.getClusters();
    }

    public Map<Sense<V>, Map<V, Number>> getSenses() {
        int i = 0;

        final Map<Sense<V>, Map<V, Number>> senses = new HashMap<>();

        for (final Collection<V> cluster : clusters) {
            final Map<V, Number> context = new HashMap<>();

            for (final V neighbor : cluster) {
                context.put(neighbor, graph.getEdgeWeight(graph.getEdge(target, neighbor)));
            }

            senses.put(new IndexedSense<>(target, i++), context);
        }

        return senses;
    }
}
