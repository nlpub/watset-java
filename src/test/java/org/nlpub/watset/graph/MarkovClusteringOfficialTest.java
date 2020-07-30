/*
 * Copyright 2020 Dmitry Ustalov
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

import java.nio.file.Path;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.nlpub.watset.graph.MarkovClusteringTest.BIPARTITE;
import static org.nlpub.watset.graph.MarkovClusteringTest.TWOCLUSTERS;

public class MarkovClusteringOfficialTest {
    Path path;

    MarkovClusteringOfficial<String, DefaultWeightedEdge> mcl1;
    MarkovClusteringOfficial<Integer, DefaultWeightedEdge> mcl2;

    @Before
    public void setup() {
        final var env = System.getenv("MCL");
        assumeTrue(nonNull(env));

        path = Path.of(env);
        assumeTrue(path.toFile().canExecute());

        mcl1 = new MarkovClusteringOfficial.Builder<String, DefaultWeightedEdge>().setPath(path).build(BIPARTITE);
        mcl2 = new MarkovClusteringOfficial.Builder<Integer, DefaultWeightedEdge>().setPath(path).build(TWOCLUSTERS);
    }

    @Test
    public void testBipartiteClustering() {
        mcl1.fit();

        final var clusters = mcl1.getClusters();
        assertEquals(2, clusters.size());
    }

    @Test
    public void testClustering() {
        mcl2.fit();

        final var clusters = mcl2.getClusters();
        assertEquals(1, clusters.size());
    }
}
