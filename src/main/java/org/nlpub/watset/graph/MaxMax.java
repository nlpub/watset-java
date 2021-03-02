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
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        protected final Set<V> roots;

        /**
         * Create an instance of the MaxMax clustering algorithm implementation.
         *
         * @param graph the graph
         */
        public Implementation(Graph<V, E> graph) {
            this.graph = graph;
            this.maximals = new HashMap<>(graph.vertexSet().size());
            this.roots = new HashSet<>(graph.vertexSet());
            this.digraph = SimpleDirectedGraph.<V, DefaultEdge>createBuilder(DefaultEdge.class).build();
        }

        /**
         * Perform clustering with MaxMax.
         *
         * @return the clustering
         */
        public MaxMaxClustering<V> compute() {
            computeMaximals();

            buildArcs();

            identifyClusters();

            final var clusters = extractClusters();

            return new MaxMaxClustering.MaxMaxClusteringImpl<>(clusters,
                    new AsUnmodifiableGraph<>(digraph),
                    Collections.unmodifiableMap(maximals),
                    Collections.unmodifiableSet(roots));
        }

        /**
         * Compute maximal vertices.
         */
        protected void computeMaximals() {
            for (final var u : graph.vertexSet()) {
                maximals.put(u, new HashSet<>());

                final var max = graph.edgesOf(u).stream().mapToDouble(graph::getEdgeWeight).max().orElse(-1);

                graph.edgesOf(u).stream().
                        filter(e -> graph.getEdgeWeight(e) == max).
                        map(e -> Graphs.getOppositeVertex(graph, e, u)).
                        forEach(v -> maximals.get(u).add(v));
            }
        }

        /**
         * Perform Stage 1: Graph Transformation.
         */
        protected void buildArcs() {
            Graphs.addAllVertices(digraph, graph.vertexSet());

            for (final var edge : graph.edgeSet()) {
                final var u = graph.getEdgeSource(edge);
                final var v = graph.getEdgeTarget(edge);

                if (maximals.get(u).contains(v)) digraph.addEdge(v, u);
                if (maximals.get(v).contains(u)) digraph.addEdge(u, v);
            }
        }

        /**
         * Perform Stage 2: Identifying Clusters.
         */
        private void identifyClusters() {
            for (final var v : digraph.vertexSet()) {
                if (roots.contains(v)) {
                    final var dfs = new DepthFirstIterator<>(digraph, v);

                    while (dfs.hasNext()) {
                        final var u = dfs.next();
                        if (!u.equals(v)) roots.remove(u);
                    }
                }
            }
        }

        /**
         * Extract clusters by traversing from the root nodes.
         *
         * @return the clusters
         */
        protected List<Set<V>> extractClusters() {
            final var clusters = new ArrayList<Set<V>>();

            for (final var root : roots) {
                final var dfs = new DepthFirstIterator<>(digraph, root);

                final var cluster = Stream.generate(() -> null).
                        takeWhile(p -> dfs.hasNext()).
                        map(n -> dfs.next()).
                        collect(Collectors.toSet());

                clusters.add(cluster);
            }

            return clusters;
        }
    }
}
