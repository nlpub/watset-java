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

import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxMaxTest {
    private final static MaxMax.Builder<String, DefaultWeightedEdge> BUILDER = MaxMax.builder();
    private final static MaxMax<String, DefaultWeightedEdge> maxmax1 = BUILDER.apply(Fixtures.MAXMAX_GRAPH);
    private final static MaxMax<String, DefaultWeightedEdge> maxmax2 = BUILDER.apply(Fixtures.FUZZY_GRAPH);

    private MaxMaxClustering<String> clustering1, clustering2;

    @Before
    public void setup() {
        clustering1 = maxmax1.getClustering();
        clustering2 = maxmax2.getClustering();
    }

    @Test
    public void testDigraphVerticesConsistency() {
        assertEquals(Fixtures.MAXMAX_GRAPH.vertexSet(), clustering1.getDigraph().vertexSet());
        assertEquals(Fixtures.FUZZY_GRAPH.vertexSet(), clustering2.getDigraph().vertexSet());
    }

    @Test
    public void testRoots() {
        assertEquals(Fixtures.MAXMAX_CLUSTERS.size(), clustering1.getRoots().size());
        assertEquals(Fixtures.FUZZY_CLUSTERS.size(), clustering2.getRoots().size());
    }

    @Test
    public void testClusters() {
        assertEquals(Fixtures.MAXMAX_CLUSTERS, clustering1.getClusters());
        assertEquals(Fixtures.FUZZY_CLUSTERS, clustering2.getClusters());
    }
}
