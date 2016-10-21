package io.github.dustalov.maxmax;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of the MaxMax word sense induction algorithm.
 *
 * @param <V> node class.
 */
public class MaxMax<V> implements Runnable {
    private final UndirectedGraph<V, DefaultWeightedEdge> graph;
    private final DirectedGraph<V, DefaultEdge> digraph;
    private final Map<V, Set<V>> maximals;
    private final Map<V, Boolean> root;

    public MaxMax(UndirectedGraph<V, DefaultWeightedEdge> graph) {
        this.graph = graph;
        this.digraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.graph.vertexSet().forEach(digraph::addVertex);
        this.maximals = this.digraph.vertexSet().stream().collect(Collectors.toMap(Function.identity(), v -> Collections.emptySet()));
        this.root = this.digraph.vertexSet().stream().collect(Collectors.toMap(Function.identity(), v -> true));
    }

    public void run() {
        // Preparation: Compute Maximal Vertices
        digraph.vertexSet().forEach(u -> {
            final double max = graph.edgesOf(u).stream().mapToDouble(graph::getEdgeWeight).max().orElse(-1);
            final Set<V> uMaximals = graph.edgesOf(u).stream().
                    filter(e -> graph.getEdgeWeight(e) == max).
                    map(e -> graph.getEdgeSource(e).equals(u) ? graph.getEdgeTarget(e) : graph.getEdgeSource(e)).
                    collect(Collectors.toSet());
            if (!uMaximals.isEmpty()) maximals.put(u, uMaximals);
        });

        // Stage 1: Graph Transformation
        graph.edgeSet().forEach(e -> {
            final V u = graph.getEdgeSource(e), v = graph.getEdgeTarget(e);
            if (maximals.get(u).contains(v)) digraph.addEdge(v, u);
            if (maximals.get(v).contains(u)) digraph.addEdge(u, v);
        });

        // Stage 2: Identifying Clusters
        final Set<V> visited = new HashSet<>();
        digraph.vertexSet().forEach(v -> {
            if (root.get(v)) {
                final Queue<V> queue = new LinkedList<>();
                queue.addAll(Graphs.successorListOf(digraph, v));
                visited.add(v);
                while (!queue.isEmpty()) {
                    final V u = queue.remove();
                    if (visited.contains(u)) continue;
                    root.put(u, false);
                    visited.add(u);
                    queue.addAll(Graphs.successorListOf(digraph, u));
                }
            }
        });
    }

    public UndirectedGraph<V, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public DirectedGraph<V, DefaultEdge> getDigraph() {
        return digraph;
    }

    public Map<V, Set<V>> getMaximals() {
        return maximals;
    }

    public Map<V, Boolean> getRoot() {
        return root;
    }

    public Set<Set<V>> getClusters() {
        final Set<V> roots = root.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toSet());
        final Set<Set<V>> clusters = roots.stream().map(root -> {
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
        return clusters;
    }
}
