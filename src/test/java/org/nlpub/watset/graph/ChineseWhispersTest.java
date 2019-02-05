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
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ChineseWhispersTest {
    private final static Random random = new Random(1337);

    public final static Graph<String, DefaultWeightedEdge> DISJOINT = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e").
            addEdge("a", "b").
            addEdge("a", "c").
            addEdge("a", "c").
            addEdge("d", "e").
            build();

    private final ChineseWhispers<String, ?> cw1 = new ChineseWhispers<>(DISJOINT, NodeWeighting.top(), ChineseWhispers.ITERATIONS, random);

    @Before
    public void setup() {
        cw1.fit();
    }

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = cw1.getClusters();
        assertEquals(2, clusters.size());
    }
}
