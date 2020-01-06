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

package org.nlpub.watset.graph;

import org.jgrapht.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * This is a weird thing. The implementation of MCL by its author is fast,
 * but is distributed under GPL. In order to use it, we need to run the separate
 * process and speak to it over standard input/output redirection.
 *
 * @see <a href="https://micans.org/mcl/">van Dongen (2000)</a>
 */
public class MarkovClusteringBinaryRunner<V, E> implements Clustering<V> {
    private static final Logger logger = Logger.getLogger(MarkovClusteringBinaryRunner.class.getSimpleName());

    private final Graph<V, E> graph;
    private final Path mcl;
    private final double r;
    private final int threads;
    private Map<V, Integer> mapping;
    private File output;

    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(Path mcl, double r, int threads) {
        return graph -> new MarkovClusteringBinaryRunner<>(graph, mcl, r, threads);
    }

    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(Path mcl, double r) {
        return graph -> new MarkovClusteringBinaryRunner<>(graph, mcl, r);
    }

    public MarkovClusteringBinaryRunner(Graph<V, E> graph, Path mcl, double r, int threads) {
        this.graph = requireNonNull(graph);
        this.mcl = mcl;
        this.r = r;
        this.threads = threads;
    }

    public MarkovClusteringBinaryRunner(Graph<V, E> graph, Path mcl, double r) {
        this(graph, mcl, r, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Collection<V>> getClusters() {
        requireNonNull(mapping, "call fit() first");
        requireNonNull(output, "call fit() first");

        final Map<Integer, V> inverse = mapping.entrySet().stream().
                collect(toMap(Map.Entry::getValue, Map.Entry::getKey));

        try {
            return Files.lines(output.toPath()).
                    map(line -> Arrays.stream(line.split("\t")).
                            map(id -> inverse.get(Integer.valueOf(id))).
                            collect(toSet())).
                    collect(toSet());
        } catch (IOException ex) {
            throw new IllegalStateException("The clusters cannot be read.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fit() {
        logger.info("Preparing for Markov Clustering.");

        mapping = translate(graph);

        try {
            process();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        logger.info("Markov Clustering finished.");
    }

    private void process() throws IOException, InterruptedException {
        output = File.createTempFile("mcl", "output");
        output.deleteOnExit();

        final File input = writeInputFile();

        final ProcessBuilder builder = new ProcessBuilder(
                mcl.toAbsolutePath().toString(),
                input.toString(),
                "-I", Double.toString(r),
                "-te", Integer.toString(threads),
                "--abc",
                "-o", output.toString());

        logger.info("Command: " + String.join(" ", builder.command()));

        final Process process = builder.start();
        int status = process.waitFor();

        if (status != 0) {
            try (final Reader isr = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)) {
                try (final BufferedReader reader = new BufferedReader(isr)) {
                    final String stderr = reader.lines().collect(Collectors.joining(System.lineSeparator()));

                    if (stderr.isEmpty()) {
                        throw new IllegalStateException(mcl.toAbsolutePath() + " returned " + status);
                    } else {
                        throw new IllegalStateException(mcl.toAbsolutePath() + " returned " + status + ": " + stderr);
                    }
                }
            }
        }
    }

    private Map<V, Integer> translate(Graph<V, E> graph) {
        final Map<V, Integer> mapping = new HashMap<>(graph.vertexSet().size());

        int i = 0;

        for (final V node : graph.vertexSet()) {
            mapping.put(node, i++);
        }

        return mapping;
    }

    private File writeInputFile() throws IOException {
        final File input = File.createTempFile("mcl", "input");
        input.deleteOnExit();

        try (final BufferedWriter writer = Files.newBufferedWriter(input.toPath())) {
            for (final E edge : graph.edgeSet()) {
                final int source = mapping.get(graph.getEdgeSource(edge));
                final int target = mapping.get(graph.getEdgeTarget(edge));
                final double weight = graph.getEdgeWeight(edge);

                writer.write(String.format(Locale.ROOT, "%d\t%d\t%f%n", source, target, weight));
            }
        }

        return input;
    }
}
