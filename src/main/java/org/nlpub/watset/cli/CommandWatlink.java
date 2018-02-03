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
import org.nlpub.watset.wsi.IndexedSense;
import org.nlpub.watset.wsi.Sense;
import org.nlpub.watset.wsi.Watlink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.nlpub.watset.io.ILEFormat.DELIMITER;

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

        final Map<String, Map<Sense<String>, Map<String, Number>>> inventory = getInventory(clusters);

        final Map<String, Collection<String>> candidates = getCandidates();

        addMissingSenses(inventory, candidates);

        final Watlink<String> watlink = new Watlink<>(inventory, new ContextCosineSimilarity<>(), k);

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
                                    map(e -> String.format(Locale.ROOT, "%s:%f", e.getKey().get(), e.getValue().doubleValue())).
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

    private Map<String, Map<Sense<String>, Map<String, Number>>> getInventory(Collection<Collection<String>> clusters) {
        final Map<String, Map<Sense<String>, Map<String, Number>>> inventory = new HashMap<>();

        int i = 0;

        for (final Collection<String> cluster : clusters) {
            for (final String word : cluster) {
                if (!inventory.containsKey(word)) inventory.put(word, new HashMap<>());

                final Map<Sense<String>, Map<String, Number>> senses = inventory.get(word);
                final Sense<String> sense = new IndexedSense<>(word, i);

                final Map<String, Number> context = cluster.stream().
                        filter(element -> !Objects.equals(word, element)).
                        collect(toMap(identity(), weight -> 1));

                senses.put(sense, context);
            }

            i++;
        }

        return inventory;
    }

    private Map<String, Collection<String>> getCandidates() {
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

    private void addMissingSenses(Map<String, Map<Sense<String>, Map<String, Number>>> inventory, Map<String, Collection<String>> candidates) {
        final Set<String> missing = new HashSet<>();

        for (final Map.Entry<String, Collection<String>> entry : candidates.entrySet()) {
            if (!inventory.containsKey(entry.getKey())) missing.add(entry.getKey());

            for (final String value : entry.getValue()) {
                if (!inventory.containsKey(value)) missing.add(value);
            }
        }

        for (final String word : missing) {
            inventory.put(word, Collections.singletonMap(new IndexedSense<>(word, 0), Collections.emptyMap()));
        }
    }
}
