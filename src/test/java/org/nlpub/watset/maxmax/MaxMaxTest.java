/*
 * Copyright 2018 Dmitry Ustalov
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

package org.nlpub.watset.maxmax;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MaxMaxTest {
    private final static Graph<String, DefaultWeightedEdge> GRAPH1 = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("r", "s", "u", "v", "t", "w", "x").
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

    private final static Set<Set<String>> CLUSTERS1 = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new HashSet<>(Arrays.asList("r", "s", "t", "u", "v")),
            new HashSet<>(Arrays.asList("w", "t", "x"))
    )));

    private final static Graph<String, DefaultWeightedEdge> GRAPH2 = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e").
            addEdge("a", "b", 3).
            addEdge("b", "c", 1).
            addEdge("c", "a", 1).
            addEdge("a", "d", 2).
            addEdge("c", "d", 1).
            addEdge("b", "e", 2).
            addEdge("c", "e", 1).
            addEdge("d", "e", 3).
            build();

    private final static Set<Set<String>> CLUSTERS2 = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new HashSet<>(Arrays.asList("a", "b", "c")),
            new HashSet<>(Arrays.asList("c", "d", "e"))
    )));

    private final MaxMax<String, DefaultWeightedEdge> maxmax1 = new MaxMax<>(GRAPH1);
    private final MaxMax<String, DefaultWeightedEdge> maxmax2 = new MaxMax<>(GRAPH2);

    @Before
    public void setup() {
        maxmax1.run();
        maxmax2.run();
    }

    @Test
    public void testGraphConsistency() {
        assertEquals(GRAPH1, maxmax1.getGraph());
        assertEquals(GRAPH2, maxmax2.getGraph());
    }

    @Test
    public void testDigraphVerticesConsistency() {
        assertEquals(GRAPH1.vertexSet(), maxmax1.getDigraph().vertexSet());
        assertEquals(GRAPH2.vertexSet(), maxmax2.getDigraph().vertexSet());
    }

    @Test
    public void testRoots() {
        assertEquals(CLUSTERS1.size(), maxmax1.getRoots().values().stream().filter(v -> v).count());
        assertEquals(CLUSTERS2.size(), maxmax2.getRoots().values().stream().filter(v -> v).count());
    }

    @Test
    public void testClusters() {
        assertEquals(CLUSTERS1, maxmax1.getClusters());
        assertEquals(CLUSTERS2, maxmax2.getClusters());
    }
}
