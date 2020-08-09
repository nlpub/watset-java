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
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Write the ILE-formatted file representing the clusters.
     *
     * @param writer     the writer
     * @param clustering the clusters
     */
    public static void write(BufferedWriter writer, ClusteringAlgorithm.Clustering<String> clustering) {
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
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
