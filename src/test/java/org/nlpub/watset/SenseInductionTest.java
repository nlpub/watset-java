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

package org.nlpub.watset;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;
import org.nlpub.cw.ChineseWhispers;
import org.nlpub.cw.weighting.ChrisWeighting;
import org.nlpub.graph.Clustering;
import org.nlpub.watset.sense.Sense;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class SenseInductionTest {
    final static Graph<String, DefaultWeightedEdge> WORDS = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e", "f", "g").
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

    private final static Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> provider = (graph) -> new ChineseWhispers<>(graph, new ChrisWeighting<>());

    private final static SenseInduction<String, DefaultWeightedEdge> senseInduction = new SenseInduction<>(WORDS, "a", provider, 1);

    @Before
    public void setup() {
        senseInduction.run();
    }

    @Test
    public void getSensesA() {
        final Map<Sense<String>, Map<String, Number>> senses = senseInduction.getSenses();
        assertEquals(3, senses.size());
    }
}
