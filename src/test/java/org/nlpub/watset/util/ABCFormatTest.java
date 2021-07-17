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

package org.nlpub.watset.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ABCFormatTest {
    final static URL EDGE_TSV = ABCFormatTest.class.getResource("edge.tsv");

    @Test
    public void testParse() throws IOException, URISyntaxException {
        try (final var edges = Files.lines(Path.of(EDGE_TSV.toURI()))) {
            final var graph = ABCFormat.parse(edges);

            assertEquals(2, graph.vertexSet().size());
            assertEquals(1, graph.edgeSet().size());
        }
    }

    @Test
    public void testParseRegex() throws URISyntaxException, IOException {
        try (final var edges = Files.lines(Path.of(EDGE_TSV.toURI()))) {
            final var graph = ABCFormat.parse(edges, " ");

            assertTrue(graph.vertexSet().isEmpty());
            assertTrue(graph.edgeSet().isEmpty());
        }
    }
}
