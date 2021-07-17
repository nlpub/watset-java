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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Path;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MarkovClusteringExternalTest {
    static Path path;

    @BeforeAll
    public static void init() {
        final var env = System.getenv("MCL");
        assumeTrue(nonNull(env));

        path = Path.of(env);
        assumeTrue(path.toFile().canExecute());
    }

    @Test
    public void testBipartiteClustering() {
        final var mcl = MarkovClusteringExternal.<String, DefaultWeightedEdge>builder().setPath(path).apply(Fixtures.BIPARTITE);
        final var clustering = mcl.getClustering();
        assertEquals(2, clustering.getNumberClusters());
    }

    @Test
    public void testClustering() {
        final var mcl = MarkovClusteringExternal.<String, DefaultWeightedEdge>builder().setPath(path).apply(Fixtures.MCL_GRAPH);
        final var clustering = mcl.getClustering();
        assertEquals(1, clustering.getNumberClusters());
    }
}
