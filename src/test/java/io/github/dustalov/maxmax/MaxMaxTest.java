package io.github.dustalov.maxmax;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxMaxTest {
    final static UndirectedGraph<String, DefaultWeightedEdge> GRAPH = SimpleWeightedGraph.<String, DefaultWeightedEdge>builder(DefaultWeightedEdge.class).
            addVertex("r").addVertex("s").addVertex("u").addVertex("v").addVertex("t").addVertex("w").addVertex("x").
            addEdge("r", "s", 3).
            addEdge("r", "v", 1).
            addEdge("r", "t", 2).
            addEdge("r", "x", 2).
            addEdge("r", "w", 2).
            addEdge("r", "u", 1).
            addEdge("x", "t", 1).
            addEdge("x", "w", 4).
            addEdge("w", "t", 2).
            addEdge("w", "v", 1).
            addEdge("w", "s", 2).
            addEdge("v", "s", 2).
            addEdge("v", "t", 1).
            addEdge("s", "t", 1).
            addEdge("s", "u", 2).
            addEdge("u", "v", 1).
            build();

    final MaxMax<String> maxmax = new MaxMax<>(GRAPH);

    @Before
    public void setup() {
        maxmax.run();
    }

    @Test
    public void testGraphConsistency() {
        assertEquals(GRAPH, maxmax.getGraph());
    }

    @Test
    public void testDigraphVerticesConsistency() {
        assertEquals(GRAPH.vertexSet(), maxmax.getDigraph().vertexSet());
    }

    @Test
    public void testRoot() {
        assertEquals(2, maxmax.getRoot().values().stream().filter(v -> v).count());
    }

    @Test
    public void testClusters() {
        assertEquals(2, maxmax.getClusters().size());
    }
}
