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

package org.nlpub.watset.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;
import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.NodeWeighting;
import org.nlpub.watset.graph.SenseInduction;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class SenseInductionTest {
    public final static Graph<String, DefaultWeightedEdge> WORDS = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e", "f", "g", "h").
            addEdge("a", "b", 10).
            addEdge("a", "c", .5).
            addEdge("a", "d").
            addEdge("a", "e", .4).
            addEdge("a", "f").
            addEdge("a", "g").
            addEdge("b", "c", 3).
            addEdge("b", "d").
            addEdge("c", "d").
            addEdge("e", "f", .25).
            build();

    private final static Random random = new Random(1337);

    private final static Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> local = ChineseWhispers.provider(NodeWeighting.top(), ChineseWhispers.ITERATIONS, random);

    private final static SenseInduction<String, DefaultWeightedEdge> senseInduction = new SenseInduction<>(WORDS, local);

    @Test
    public void getSensesA() {
        final List<Map<String, Number>> senses = senseInduction.contexts("a");
        assertEquals(3, senses.size());
    }
}
