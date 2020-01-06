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
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the MaxMax clustering algorithm.
 *
 * @param <V> node class.
 * @param <E> edge class.
 * @see <a href="https://doi.org/10.1007/978-3-642-37247-6_30">Hope &amp; Keller (CICLing 2013)</a>
 */
public class MaxMax<V, E> implements Clustering<V> {
    /**
     * Sets up the MaxMax clustering algorithm in a functional style.
     *
     * @param <V> node class.
     * @param <E> edge class.
     * @return an instance of MaxMax.
     */
    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider() {
        return MaxMax::new;
    }

    private final Graph<V, E> graph;
    private Graph<V, DefaultEdge> digraph;
    private Map<V, Set<V>> maximals;
    private Map<V, Boolean> roots;

    /**
     * Sets up the MaxMax clustering algorithm.
     *
     * @param graph an input graph.
     */
    public MaxMax(Graph<V, E> graph) {
        this.graph = requireNonNull(graph);
    }

    @Override
    public void fit() {
        digraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        maximals = null;
        roots = null;

        graph.vertexSet().forEach(digraph::addVertex);

        // Preparation: Compute Maximal Vertices
        maximals = this.digraph.vertexSet().stream().collect(Collectors.toMap(Function.identity(), v -> new HashSet<>()));

        digraph.vertexSet().forEach(u -> {
            final double max = graph.edgesOf(u).stream().mapToDouble(graph::getEdgeWeight).max().orElse(-1);
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

        final Set<V> visited = new HashSet<>();

        digraph.vertexSet().forEach(v -> {
            if (roots.get(v)) {
                final Queue<V> queue = new LinkedList<>(Graphs.successorListOf(digraph, v));
                visited.add(v);
                while (!queue.isEmpty()) {
                    final V u = queue.remove();
                    if (visited.contains(u)) continue;
                    roots.put(u, false);
                    visited.add(u);
                    queue.addAll(Graphs.successorListOf(digraph, u));
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Collection<V>> getClusters() {
        requireNonNull(roots, "call fit() first");

        final Set<V> roots = this.roots.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toSet());

        return roots.stream().map(root -> {
            final Set<V> visited = new HashSet<>();

            final Queue<V> queue = new LinkedList<>();
            queue.add(root);

            while (!queue.isEmpty()) {
                final V v = queue.remove();
                if (visited.contains(v)) continue;
                visited.add(v);
                queue.addAll(Graphs.successorListOf(digraph, v));
            }

            return visited;
        }).collect(Collectors.toSet());
    }

    /**
     * Returns the directed graph representation of the input graph.
     *
     * @return a directed graph.
     */
    @SuppressWarnings("WeakerAccess")
    public Graph<V, DefaultEdge> getDigraph() {
        return new AsUnmodifiableGraph<>(requireNonNull(digraph, "call fit() first"));
    }

    /**
     * Returns the map of nodes to their maximal affinity nodes.
     *
     * @return a map of maximal affinities.
     */
    @SuppressWarnings("unused")
    public Map<V, Set<V>> getMaximals() {
        return Collections.unmodifiableMap(requireNonNull(maximals, "call fit() first"));
    }

    /**
     * Return the map of root and non-root nodes.
     *
     * @return a map of root and non-root nodes.
     */
    @SuppressWarnings("WeakerAccess")
    public Map<V, Boolean> getRoots() {
        return Collections.unmodifiableMap(requireNonNull(roots, "call fit() first"));
    }
}
