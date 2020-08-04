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
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static class Builder<V, E> implements ClusteringBuilder<V, E, MaxMax<V, E>> {
        @Override
        public MaxMax<V, E> build(Graph<V, E> graph) {
            return new MaxMax<>(graph);
        }

        @Override
        public Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
            return MaxMax.provider();
        }
    }

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    public static <V, E> Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
        return MaxMax::new;
    }

    private final Graph<V, E> graph;
    private Graph<V, DefaultEdge> digraph;
    private Map<V, Set<V>> maximals;
    private Map<V, Boolean> roots;

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
        digraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        maximals = null;
        roots = null;

        Graphs.addAllVertices(digraph, graph.vertexSet());

        // Preparation: Compute Maximal Vertices
        maximals = digraph.vertexSet().stream().collect(Collectors.toMap(Function.identity(), v -> new HashSet<>()));

        digraph.vertexSet().forEach(u -> {
            final var max = graph.edgesOf(u).stream().mapToDouble(graph::getEdgeWeight).max().orElse(-1);

            graph.edgesOf(u).stream().
                    filter(e -> graph.getEdgeWeight(e) == max).
                    map(e -> Graphs.getOppositeVertex(graph, e, u)).
                    forEach(v -> maximals.get(u).add(v));
        });

        // Stage 1: Graph Transformation
        graph.edgeSet().forEach(e -> {
            final V u = graph.getEdgeSource(e), v = graph.getEdgeTarget(e);
            if (maximals.get(u).contains(v)) digraph.addEdge(v, u);
            if (maximals.get(v).contains(u)) digraph.addEdge(u, v);
        });

        // Stage 2: Identifying Clusters
        roots = this.digraph.vertexSet().stream().collect(Collectors.toMap(Function.identity(), v -> true));

        final var visited = new HashSet<V>();

        digraph.vertexSet().forEach(v -> {
            if (roots.get(v)) {
                final var queue = new LinkedList<>(Graphs.successorListOf(digraph, v));

                visited.add(v);

                while (!queue.isEmpty()) {
                    final var u = queue.remove();

                    if (visited.contains(u)) continue;

                    roots.put(u, false);

                    visited.add(u);

                    queue.addAll(Graphs.successorListOf(digraph, u));
                }
            }
        });

        // Final: Retrieving Clusters
        final var rootNodes = this.roots.entrySet().stream().
                filter(Map.Entry::getValue).
                map(Map.Entry::getKey).
                collect(Collectors.toSet());

        final var clusters = rootNodes.stream().map(root -> {
            final var cluster = new HashSet<V>();

            final var queue = new LinkedList<V>();
            queue.add(root);

            while (!queue.isEmpty()) {
                final var v = queue.remove();

                if (cluster.contains(v)) continue;

                cluster.add(v);

                queue.addAll(Graphs.successorListOf(digraph, v));
            }

            return (Set<V>) cluster;
        }).collect(Collectors.toList());

        return new MaxMaxClustering.MaxMaxClusteringImpl<>(clusters,
                new AsUnmodifiableGraph<>(digraph),
                Collections.unmodifiableMap(maximals),
                Collections.unmodifiableMap(roots));
    }
}
