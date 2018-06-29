/*
 * Copyright 2018 Dmitry Ustalov
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

package org.nlpub.watset.cli;

import com.beust.jcommander.Parameter;
import org.nlpub.watset.vsm.ContextCosineSimilarity;
import org.nlpub.watset.wsi.Sense;
import org.nlpub.watset.wsi.Watlink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.nlpub.watset.io.ILEFormat.DELIMITER;
import static org.nlpub.watset.wsi.Watlink.makeInventory;

class CommandWatlink implements Runnable {
    private final Application application;

    @Parameter(required = true, names = "-k")
    private Integer k;

    @Parameter(required = true, names = {"-c", "--candidates"}, converter = PathConverter.class)
    private Path candidates;

    public CommandWatlink(Application application) {
        this.application = application;
    }

    @Override
    public void run() {
        final Collection<Collection<String>> clusters = application.getClusters();
        final Map<String, Map<Sense<String>, Map<String, Number>>> inventory = makeInventory(clusters);

        final Watlink<String> watlink = new Watlink<>(inventory, new ContextCosineSimilarity<>(), k);
        final Map<String, Collection<String>> candidates = readCandidates();
        watlink.addMissingSenses(candidates);

        try (final BufferedWriter writer = Files.newBufferedWriter(application.output)) {
            final AtomicInteger counter = new AtomicInteger(0);

            clusters.parallelStream().forEach(cluster -> {
                final Map<Sense<String>, Number> dcontext = watlink.retrieve(cluster, candidates);

                try {
                    writer.write(String.format(Locale.ROOT, "%d\t%d\t%s\t%d\t%s\n",
                            counter.incrementAndGet(),
                            cluster.size(),
                            String.join(DELIMITER, cluster),
                            dcontext.size(),
                            dcontext.entrySet().stream().
                                    map(e -> String.format(Locale.ROOT, "%s:%f", e.getKey(), e.getValue().doubleValue())).
                                    collect(joining(DELIMITER)))
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Collection<String>> readCandidates() {
        try (final Stream<String> stream = Files.lines(candidates)) {
            final Map<String, Collection<String>> candidates = new HashMap<>();

            stream.forEach(line -> {
                final String[] split = line.split("\t");

                if (split.length < 2) return;

                if (!candidates.containsKey(split[0])) candidates.put(split[0], new HashSet<>());

                final Set<String> upper = (Set<String>) candidates.get(split[0]);

                upper.add(split[1]);
            });

            return candidates;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
