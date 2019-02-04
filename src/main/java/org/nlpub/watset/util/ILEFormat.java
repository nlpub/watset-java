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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utilities to handle the ILE (identifier, length, elements) file format.
 */
public interface ILEFormat {
    String SEPARATOR = "\t";
    String DELIMITER = ", ";

    static Collection<Collection<String>> parse(Stream<String> stream, String regex) {
        return stream.map(line -> {
            final String[] split = line.split(regex);

            if (split.length < 2) return null;

            return Arrays.asList(split[2].split(DELIMITER));
        }).filter(Objects::nonNull).collect(toList());
    }

    static Collection<Collection<String>> parse(Stream<String> stream) {
        return parse(stream, SEPARATOR);
    }

    static void write(Path path, Clustering<String> clustering) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            final AtomicInteger counter = new AtomicInteger(0);

            clustering.getClusters().stream().
                    sorted((smaller, larger) -> Integer.compare(larger.size(), smaller.size())).
                    forEach(cluster -> {
                        try {
                            writer.write(String.format(Locale.ROOT, "%d\t%d\t%s\n",
                                    counter.incrementAndGet(),
                                    cluster.size(),
                                    String.join(DELIMITER, cluster))
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
