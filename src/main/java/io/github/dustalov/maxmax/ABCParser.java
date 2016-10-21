package io.github.dustalov.maxmax;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.stream.Stream;

interface ABCParser {
    static UndirectedGraph<String, DefaultWeightedEdge> parse(Stream<String> stream) {
        final SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        stream.forEach(line -> {
            final String[] split = line.split("\t");
            if (split.length != 3) return;
            final Double weight = Double.valueOf(split[2]);
            if (split[0].equals(split[1])) return;
            if (!graph.containsVertex(split[0])) graph.addVertex(split[0]);
            if (!graph.containsVertex(split[1])) graph.addVertex(split[1]);
            final DefaultWeightedEdge edge = graph.addEdge(split[0], split[1]);
            if (edge != null) graph.setEdgeWeight(edge, weight);
        });
        return graph;
    }
}
