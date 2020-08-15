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

public class WatsetTest {
    private final static Random random = new Random(1337);

    private final static ChineseWhispers.Builder<String, DefaultWeightedEdge> local = ChineseWhispers.<String, DefaultWeightedEdge>builder().setRandom(random);

    private final static ChineseWhispers.Builder<Sense<String>, DefaultWeightedEdge> global = ChineseWhispers.<Sense<String>, DefaultWeightedEdge>builder().setRandom(random);

    private final static Watset<String, DefaultWeightedEdge> watset = Watset.<String, DefaultWeightedEdge>builder().setLocal(local).setGlobal(global).apply(SenseInductionTest.WORDS);

    @Test
    public void testClustering() {
        final var clustering = watset.getClustering();
        assertEquals(4, clustering.getNumberClusters());
    }
}
