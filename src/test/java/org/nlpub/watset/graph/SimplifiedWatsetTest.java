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
import org.junit.Test;
import org.nlpub.watset.util.Sense;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SimplifiedWatsetTest {
    private final static Random random = new Random(1337);

    private final static ChineseWhispers.Builder<String, DefaultWeightedEdge> localBuilder = new ChineseWhispers.Builder<String, DefaultWeightedEdge>().setRandom(random);

    private final static ChineseWhispers.Builder<Sense<String>, DefaultWeightedEdge> globalBuilder = new ChineseWhispers.Builder<Sense<String>, DefaultWeightedEdge>().setRandom(random);

    private final static SimplifiedWatset<String, DefaultWeightedEdge> watset = new SimplifiedWatset.Builder<String, DefaultWeightedEdge>().setLocalBuilder(localBuilder).setGlobalBuilder(globalBuilder).build(SenseInductionTest.WORDS);

    @Test
    public void testClustering() {
        final var clustering = watset.getClustering();
        assertEquals(4, clustering.getNumberClusters());
    }
}
