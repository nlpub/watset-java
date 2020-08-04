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
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A MaxMax clustering.
 *
 * @param <V> the type of nodes in the graph
 */
@SuppressWarnings("unused")
public interface MaxMaxClustering<V> extends ClusteringAlgorithm.Clustering<V> {
    /**
     * Return the directed graph representation of the input graph.
     *
     * @return a directed graph
     */
    Graph<V, DefaultEdge> getDigraph();

    /**
     * Return the map of nodes to their maximal affinity nodes.
     *
     * @return a map of maximal affinities
     */
    Map<V, Set<V>> getMaximals();

    /**
     * Return the map of root and non-root nodes.
     *
     * @return a map of root and non-root nodes
     */
    Map<V, Boolean> getRoots();

    /**
     * Default implementation of the MaxMax clustering.
     *
     * @param <V> the type of nodes in the graph
     */
    class MaxMaxClusteringImpl<V> extends ClusteringAlgorithm.ClusteringImpl<V> implements MaxMaxClustering<V> {
        private final Graph<V, DefaultEdge> digraph;
        private final Map<V, Set<V>> maximals;
        private final Map<V, Boolean> roots;

        /**
         * Construct a new MaxMax clustering.
         *
         * @param clusters the clusters
         * @param digraph  the directed graph representation
         * @param maximals the map of maximal affinities
         * @param roots    the map of root and non-root nodes
         */
        public MaxMaxClusteringImpl(List<Set<V>> clusters, Graph<V, DefaultEdge> digraph, Map<V, Set<V>> maximals, Map<V, Boolean> roots) {
            super(clusters);
            this.digraph = digraph;
            this.maximals = maximals;
            this.roots = roots;
        }

        @Override
        public Graph<V, DefaultEdge> getDigraph() {
            return digraph;
        }

        @Override
        public Map<V, Set<V>> getMaximals() {
            return maximals;
        }

        @Override
        public Map<V, Boolean> getRoots() {
            return roots;
        }
    }
}
