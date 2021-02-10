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

public class MarkovClusteringExternalTest {
    Path path;

    MarkovClusteringExternal<String, DefaultWeightedEdge> mcl1;
    MarkovClusteringExternal<String, DefaultWeightedEdge> mcl2;

    @Before
    public void setup() {
        final var env = System.getenv("MCL");
        assumeTrue(nonNull(env));

        path = Path.of(env);
        assumeTrue(path.toFile().canExecute());

        mcl1 = MarkovClusteringExternal.<String, DefaultWeightedEdge>builder().setPath(path).apply(Fixtures.BIPARTITE);
        mcl2 = MarkovClusteringExternal.<String, DefaultWeightedEdge>builder().setPath(path).apply(Fixtures.MCL_GRAPH);
    }

    @Test
    public void testBipartiteClustering() {
        final var clustering = mcl1.getClustering();
        assertEquals(2, clustering.getNumberClusters());
    }

    @Test
    public void testClustering() {
        final var clustering = mcl2.getClustering();
        assertEquals(1, clustering.getNumberClusters());
    }
}
