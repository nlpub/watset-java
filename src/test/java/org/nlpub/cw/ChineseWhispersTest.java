/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.cw;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;
import org.nlpub.cw.weighting.ChrisWeighting;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ChineseWhispersTest {
    public final static Graph<String, DefaultWeightedEdge> DISJOINT = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e").
            addEdge("a", "b").
            addEdge("a", "c").
            addEdge("a", "c").
            addEdge("d", "e").
            build();

    final ChineseWhispers<String, ?> cw1 = new ChineseWhispers<>(DISJOINT, new ChrisWeighting<>());

    @Before
    public void setup() {
        cw1.run();
    }

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = cw1.getClusters();
        assertEquals(2, clusters.size());
    }
}
