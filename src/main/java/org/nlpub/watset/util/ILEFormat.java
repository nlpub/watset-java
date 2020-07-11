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

import org.nlpub.watset.graph.Clustering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilities for handling the ILE {@code (identifier, length, elements)} file format.
 */
public interface ILEFormat {
    /**
     * The default separator, expressed by the tab symbol.
     */
    String SEPARATOR = "\t";

    /**
     * The default delimiter, expressed by comma and space.
     */
    String DELIMITER = ", ";

    /**
     * Write the ILE-formatted file representing the clusters.
     *
     * @param path       the path
     * @param clustering the clusters
     * @throws IOException if an I/O error occurs
     */
    static void write(Path path, Clustering<String> clustering) throws IOException {
        try (final var writer = Files.newBufferedWriter(path)) {
            final var counter = new AtomicInteger(0);

            clustering.getClusters().stream().
                    sorted((smaller, larger) -> Integer.compare(larger.size(), smaller.size())).
                    forEach(cluster -> {
                        try {
                            writer.write(String.format(Locale.ROOT, "%d%s%d%s%s%n",
                                    counter.incrementAndGet(),
                                    SEPARATOR,
                                    cluster.size(),
                                    SEPARATOR,
                                    String.join(DELIMITER, cluster))
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}