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

package org.nlpub.watset.wsi;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;
import org.nlpub.watset.cw.ChineseWhispers;
import org.nlpub.watset.cw.LabelSelector;
import org.nlpub.watset.cw.NodeWeighting;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.vsm.ContextCosineSimilarity;

import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class WatsetTest {
    private final static Random random = new Random(1337);
    final static Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> localClusteringProvider = ego -> new ChineseWhispers<>(ego, NodeWeighting.top(), LabelSelector.total(), ChineseWhispers.ITERATIONS, random);
    final static Function<Graph<Sense<String>, DefaultWeightedEdge>, Clustering<Sense<String>>> globalClusteringProvider = ego -> new ChineseWhispers<>(ego, NodeWeighting.top(), LabelSelector.total(), ChineseWhispers.ITERATIONS, random);
    private final static Watset<String, DefaultWeightedEdge> watset = new Watset<>(SenseInductionTest.WORDS, localClusteringProvider, globalClusteringProvider, new ContextCosineSimilarity<>());

    @Before
    public void setup() {
        watset.run();
    }

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = watset.getClusters();
        assertEquals(3, clusters.size());
    }
}
