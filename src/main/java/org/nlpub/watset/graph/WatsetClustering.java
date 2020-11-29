/*
 * Copyright 2020 Dmitry Ustalov
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

package org.nlpub.watset.graph;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.Sense;

import java.util.*;

import static java.util.Objects.isNull;

/**
 * A Watset clustering.
 *
 * @param <V> the type of nodes in the graph
 */
public interface WatsetClustering<V> extends ClusteringAlgorithm.Clustering<V> {
    /**
     * Get the intermediate node sense graph built during {@link Watset#getClustering()}.
     *
     * @return the sense graph
     */
    Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph();

    /**
     * Get the disambiguated contexts built during {@link Watset#getClustering()}.
     *
     * @return the disambiguated contexts
     */
    Map<Sense<V>, Map<Sense<V>, Number>> getContexts();

    /**
     * A Watset clustering that computes disambiguated contexts on demand.
     *
     * @param <V> the type of nodes in the graph
     */
    class WatsetClusteringImpl<V> extends ClusteringAlgorithm.ClusteringImpl<V> implements WatsetClustering<V> {
        /**
         * The sense inventory.
         */
        private final Map<V, Map<V, Integer>> inventory;

        /**
         * The sense graph.
         */
        private final Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

        /**
         * The cached disambiguated contexts.
         */
        private Map<Sense<V>, Map<Sense<V>, Number>> contexts;

        /**
         * Construct a new Watset clustering.
         *
         * @param clusters   the clusters
         * @param inventory  the sense inventory
         * @param senseGraph the sense graph
         */
        public WatsetClusteringImpl(List<Set<V>> clusters, Map<V, Map<V, Integer>> inventory, Graph<Sense<V>, DefaultWeightedEdge> senseGraph) {
            super(clusters);
            this.inventory = inventory;
            this.senseGraph = senseGraph;
        }

        /**
         * Get the sense inventory built during {@link Watset#getClustering()}.
         *
         * @return the sense inventory
         */
        @SuppressWarnings("unused")
        public Map<V, Map<V, Integer>> getInventory() {
            return inventory;
        }

        @Override
        public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
            return senseGraph;
        }

        @Override
        public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
            if (isNull(contexts)) {
                contexts = new HashMap<>(senseGraph.vertexSet().size());

                for (final var edge : senseGraph.edgeSet()) {
                    final Sense<V> source = senseGraph.getEdgeSource(edge), target = senseGraph.getEdgeTarget(edge);
                    final var weight = senseGraph.getEdgeWeight(edge);

                    if (!contexts.containsKey(source)) contexts.put(source, new HashMap<>());
                    if (!contexts.containsKey(target)) contexts.put(target, new HashMap<>());

                    contexts.get(source).put(target, weight);
                    contexts.get(target).put(source, weight);
                }

                if (contexts.size() != senseGraph.vertexSet().size()) {
                    throw new IllegalStateException("Mismatch in number of senses: expected " +
                            senseGraph.vertexSet().size() +
                            ", but got " +
                            contexts.size());
                }
            }

            return Collections.unmodifiableMap(contexts);
        }
    }
}
