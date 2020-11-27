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

import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utilities for handling the ILE {@code (identifier, length, elements)} file format.
 */
public final class ILEFormat {
    private ILEFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The default separator, expressed by the tab symbol.
     */
    public static final String SEPARATOR = "\t";

    /**
     * The default delimiter, expressed by comma and space.
     */
    public static final String DELIMITER = ", ";

    /**
     * Read the ILE-formatted file representing the clusters.
     *
     * @param stream the input stream
     * @return the clusters
     * @throws IOException if an I/O error occurs
     */
    public static ClusteringAlgorithm.Clustering<String> read(Stream<String> stream) throws IOException {
        final var clusters = new ArrayList<Set<String>>();

        stream.forEach(line -> {
            final var fields = line.split(SEPARATOR, 3);
            final var length = Integer.valueOf(fields[1]);
            final var split = fields[2].split(DELIMITER, length);
            clusters.add(Set.of(split));
        });

        return new ClusteringAlgorithm.ClusteringImpl<>(clusters);
    }

    /**
     * Write the ILE-formatted file representing the clusters.
     *
     * @param writer     the writer
     * @param clustering the clusters
     * @throws IOException if an I/O error occurs
     */
    public static void write(BufferedWriter writer, ClusteringAlgorithm.Clustering<String> clustering) throws IOException {
        final Iterable<Set<String>> clusters = () -> clustering.getClusters().stream().
                sorted((smaller, larger) -> Integer.compare(larger.size(), smaller.size())).
                iterator();

        int counter = 0;

        for (final var cluster : clusters) {
            writer.write(Integer.toString(++counter));
            writer.write(SEPARATOR);
            writer.write(Integer.toString(cluster.size()));
            writer.write(SEPARATOR);
            writer.write(String.join(DELIMITER, cluster));
            writer.write(System.lineSeparator());
        }
    }
}
