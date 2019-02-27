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
import org.junit.Before;
import org.junit.Test;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.wsi.Sense;
import org.nlpub.watset.wsi.SenseInductionTest;

import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class WatsetTest {
    private final static Random random = new Random(1337);
    public final static Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> local = ChineseWhispers.provider(NodeWeighting.top(), ChineseWhispers.ITERATIONS, random);
    public final static Function<Graph<Sense<String>, DefaultWeightedEdge>, Clustering<Sense<String>>> global = ChineseWhispers.provider(NodeWeighting.top(), ChineseWhispers.ITERATIONS, random);
    private final static Watset<String, DefaultWeightedEdge> watset = new Watset<>(SenseInductionTest.WORDS, local, global, new CosineContextSimilarity<>());

    @Before
    public void setup() {
        watset.fit();
    }

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = watset.getClusters();
        assertEquals(4, clusters.size());
    }
}
