/*
 * Copyright 2019 Dmitry Ustalov
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
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

import static java.util.Objects.isNull;
import static org.jgrapht.GraphTests.requireUndirected;
import static org.jgrapht.GraphTests.requireWeighted;

/**
 * Implementation of the MaxMax soft clustering algorithm.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1007/978-3-642-37247-6_30">Hope &amp; Keller (CICLing 2013)</a>
 */
public class MaxMax<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link MaxMax}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, MaxMax<V, E>> {
        @Override
        public MaxMax<V, E> apply(Graph<V, E> graph) {
            return new MaxMax<>(graph);
        }
    }

    /**
     * Create a builder.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a builder
     */
    public static <V, E> Builder<V, E> builder() {
        return new Builder<>();
    }

    /**
     * The graph.
     */
    private final Graph<V, E> graph;

    /**
     * The cached clustering result.
     */
    private MaxMaxClustering<V> clustering;

    /**
     * Create an instance of the MaxMax algorithm.
     *
     * @param graph the graph
     */
    public MaxMax(Graph<V, E> graph) {
        this.graph = requireWeighted(requireUndirected(graph));

        if (!GraphTests.isSimple(graph)) {
            throw new IllegalArgumentException("Graph must be simple");
        }
    }

    @Override
    public MaxMaxClustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph).compute();
        }

        return clustering;
    }

    /**
     * Actual implementation of MaxMax.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    protected static class Implementation<V, E> {
        /**
         * The graph.
         */
        protected final Graph<V, E> graph;

        /**
         * The weights.
         */
        protected final Map<V, Double> weights;

        /**
         * The directed graph.
         */
        protected final Graph<V, DefaultEdge> digraph;

        /**
         * Create an instance of the MaxMax clustering algorithm implementation.
         *
         * @param graph the graph
         */
        public Implementation(Graph<V, E> graph) {
            this.graph = graph;
            this.weights = new HashMap<>(graph.vertexSet().size());
            this.digraph = SimpleDirectedGraph.<V, DefaultEdge>createBuilder(DefaultEdge.class).build();
        }

        /**
         * Perform clustering with MaxMax.
         *
         * @return the clustering
         */
        public MaxMaxClustering<V> compute() {
            computeWeights();

            buildArcs();

            final var clusters = extractClusters();

            final long elements = clusters.values().stream().flatMap(Collection::stream).distinct().count();

            if (elements != graph.vertexSet().size()) {
                throw new IllegalStateException("Clusters do not cover the nodes: " + elements + " vs. " + graph.vertexSet().size());
            }

            return new MaxMaxClustering.MaxMaxClusteringImpl<>(List.copyOf(clusters.values()),
                    new AsUnmodifiableGraph<>(digraph),
                    Collections.unmodifiableSet(clusters.keySet()));
        }

        /**
         * Compute the weights for maximal affinity nodes.
         */
        private void computeWeights() {
            for (final var edge : graph.edgeSet()) {
                final var u = graph.getEdgeSource(edge);
                final var v = graph.getEdgeTarget(edge);
                final var weight = graph.getEdgeWeight(edge);

                if ((!weights.containsKey(u)) || (weights.get(u) < weight)) weights.put(u, weight);
                if ((!weights.containsKey(v)) || (weights.get(v) < weight)) weights.put(v, weight);
            }

            if (!weights.keySet().equals(graph.vertexSet())) {
                throw new IllegalArgumentException("Graph must not have zero-degree nodes");
            }
        }

        /**
         * Build the intermediate directed graph.
         */
        protected void buildArcs() {
            for (final var edge : graph.edgeSet()) {
                final var u = graph.getEdgeSource(edge);
                final var v = graph.getEdgeTarget(edge);
                final var weight = graph.getEdgeWeight(edge);

                if (weight == weights.get(u)) Graphs.addEdgeWithVertices(digraph, v, u);
                if (weight == weights.get(v)) Graphs.addEdgeWithVertices(digraph, u, v);
            }
        }

        /**
         * Extract the quasi-strongly connected subgraphs from the intermediate directed graph.
         *
         * @return the map of cluster roots to the clusters
         */
        protected Map<V, Set<V>> extractClusters() {
            final var leaves = new HashSet<>(graph.vertexSet().size());
            final var clusters = new HashMap<V, Set<V>>();

            for (final var u : digraph.vertexSet()) {
                if (!leaves.contains(u)) {
                    final var cluster = new HashSet<V>();

                    cluster.add(u);

                    final var queue = new ArrayDeque<>(Graphs.successorListOf(digraph, u));

                    final var visited = new HashSet<V>();

                    while (!queue.isEmpty()) {
                        final var v = queue.remove();
                        leaves.add(v);

                        if (!visited.contains(v)) {
                            clusters.remove(v);

                            if (digraph.containsVertex(v)) queue.addAll(Graphs.successorListOf(digraph, v));

                            cluster.add(v);
                            visited.add(v);
                        }
                    }

                    clusters.put(u, cluster);
                }
            }

            return clusters;
        }
    }
}
