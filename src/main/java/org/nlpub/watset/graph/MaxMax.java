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
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.jgrapht.GraphTests.requireUndirected;

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
        this.graph = requireUndirected(graph);
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
         * The map of nodes to their maximal affinity nodes.
         */
        protected final Map<V, Set<V>> maximals;

        /**
         * The directed graph.
         */
        protected final Graph<V, DefaultEdge> digraph;

        /**
         * The map of root and non-root nodes.
         */
        protected final Map<V, Boolean> roots;

        /**
         * Create an instance of the MaxMax clustering algorithm implementation.
         *
         * @param graph the graph
         */
        public Implementation(Graph<V, E> graph) {
            this.graph = graph;
            this.maximals = new HashMap<>(graph.vertexSet().size());
            this.roots = new HashMap<>(graph.vertexSet().size());

            final var builder = SimpleDirectedGraph.<V, DefaultEdge>createBuilder(DefaultEdge.class);

            for (final var node : graph.vertexSet()) {
                maximals.put(node, new HashSet<>());
                roots.put(node, true);
                builder.addVertex(node);
            }

            this.digraph = builder.build();
        }

        /**
         * Perform clustering with MaxMax.
         *
         * @return the clustering
         */
        public MaxMaxClustering<V> compute() {
            // Preparation: Compute Maximal Vertices
            for (final var u : digraph.vertexSet()) {
                final var max = graph.edgesOf(u).stream().mapToDouble(graph::getEdgeWeight).max().orElse(-1);

                graph.edgesOf(u).stream().
                        filter(e -> graph.getEdgeWeight(e) == max).
                        map(e -> Graphs.getOppositeVertex(graph, e, u)).
                        forEach(v -> maximals.get(u).add(v));
            }

            // Stage 1: Graph Transformation
            for (final var e : graph.edgeSet()) {
                final var u = graph.getEdgeSource(e);
                final var v = graph.getEdgeTarget(e);

                if (maximals.get(u).contains(v)) digraph.addEdge(v, u);
                if (maximals.get(v).contains(u)) digraph.addEdge(u, v);
            }

            // Stage 2: Identifying Clusters
            final var visited = new HashSet<V>();

            for (final var v : digraph.vertexSet()) {
                if (roots.get(v)) {
                    final var queue = new ArrayDeque<>(Graphs.successorListOf(digraph, v));

                    visited.add(v);

                    while (!queue.isEmpty()) {
                        final var u = queue.remove();

                        if (!visited.contains(u)) {
                            roots.put(u, false);
                            visited.add(u);
                            queue.addAll(Graphs.successorListOf(digraph, u));
                        }
                    }
                }
            }

            final var clusters = extractClusters();

            return new MaxMaxClustering.MaxMaxClusteringImpl<>(clusters,
                    new AsUnmodifiableGraph<>(digraph),
                    Collections.unmodifiableMap(maximals),
                    Collections.unmodifiableMap(roots));
        }

        /**
         * Extract clusters by traversing from the root nodes.
         *
         * @return the clusters
         */
        protected List<Set<V>> extractClusters() {
            final var rootNodes = roots.entrySet().stream().
                    filter(Map.Entry::getValue).
                    map(Map.Entry::getKey).
                    collect(Collectors.toSet());

            return rootNodes.stream().map(root -> {
                final Set<V> cluster = new HashSet<>();

                final var queue = new ArrayDeque<V>();
                queue.add(root);

                while (!queue.isEmpty()) {
                    final var v = queue.remove();

                    if (!cluster.contains(v)) {
                        cluster.add(v);
                        queue.addAll(Graphs.successorListOf(digraph, v));
                    }
                }

                return cluster;
            }).collect(Collectors.toList());
        }
    }
}
