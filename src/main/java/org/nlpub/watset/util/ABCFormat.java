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

package org.nlpub.watset.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.stream.Stream;

/**
 * Utilities for handling the ABC {@code (source, target, weight)} edge list format.
 */
public final class ABCFormat {
    private ABCFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The default separator, expressed by the tab symbol.
     */
    public static final String SEPARATOR = "\t";

    /**
     * Parse the string stream of ABC-formatted edges.
     *
     * @param stream the input stream
     * @param regex  the separator regular expression
     * @return the graph represented in the stream
     */
    public static Graph<String, DefaultWeightedEdge> parse(Stream<String> stream, String regex) {
        final var builder = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class);

        stream.forEach(line -> {
            final var split = line.split(regex, -1);

            if (split.length != 3 || split[0].equals(split[1])) return;

            builder.addVertices(split[0], split[1]);

            builder.addEdge(split[0], split[1], Double.parseDouble(split[2]));
        });

        return builder.build();
    }

    /**
     * Parse the string stream of ABC-formatted edges.
     *
     * @param stream the input stream
     * @return the graph represented in the stream
     */
    public static Graph<String, DefaultWeightedEdge> parse(Stream<String> stream) {
        return parse(stream, SEPARATOR);
    }
}
