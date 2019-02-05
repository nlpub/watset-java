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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class MarkovClusteringTest {
    private final static Graph<String, DefaultWeightedEdge> BIPARTITE = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("0", "1", "2", "3", "4", "5", "6", "7").
            addEdge("0", "1").
            addEdge("0", "2").
            addEdge("0", "3").
            addEdge("1", "2").
            addEdge("1", "3").
            addEdge("2", "3").
            addEdge("2", "6").
            addEdge("4", "5").
            addEdge("4", "6").
            addEdge("4", "7").
            addEdge("5", "6").
            addEdge("5", "7").
            addEdge("6", "7").
            build();

    /**
     * Example from https://www.cs.ucsb.edu/~xyan/classes/CS595D-2009winter/MCL_Presentation2.pdf.
     */
    private final static Graph<Integer, DefaultWeightedEdge> TWOCLUSTERS = SimpleWeightedGraph.<Integer, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices(1, 2, 3, 4).
            addEdge(1, 2).
            addEdge(1, 3).
            addEdge(1, 4).
            addEdge(2, 4).
            build();

    private final MarkovClustering<String, ?> mcl1 = new MarkovClustering<>(BIPARTITE, 2, 2);
    private final MarkovClustering<Integer, ?> mcl2 = new MarkovClustering<>(TWOCLUSTERS, 2, 2);

    @Before
    public void setup() {
        mcl1.fit();
        mcl2.fit();
    }

    @Test
    public void testBipartiteClustering() {
        final Collection<Collection<String>> clusters = mcl1.getClusters();
        assertEquals(2, clusters.size());
    }

    @Test
    public void testClustering() {
        final Collection<Collection<Integer>> clusters = mcl2.getClusters();
        assertEquals(1, clusters.size());
    }
}
