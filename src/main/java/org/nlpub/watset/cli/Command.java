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

package org.nlpub.watset.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ABCFormat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * A generic command of the Watset command-line interface.
 */
public abstract class Command implements Runnable {
    /**
     * Watset command-line interface parameters.
     */
    @SuppressWarnings("unused")
    public static class MainParameters {
        /**
         * The input file.
         */
        @Parameter(names = {"-i", "--input"}, description = "Input file", converter = PathConverter.class)
        public Path input;

        /**
         * The output file.
         */
        @Parameter(names = {"-o", "--output"}, description = "Output file", converter = PathConverter.class)
        public Path output;
    }

    /**
     * Local clustering command-line interface parameters.
     */
    @SuppressWarnings("unused")
    public static class LocalParameters {
        /**
         * The local clustering algorithm.
         */
        @Parameter(required = true, description = "Local clustering algorithm", names = {"-l", "--local"})
        public String algorithm;

        /**
         * The local clustering algorithm parameters.
         */
        @DynamicParameter(description = "Local clustering algorithm parameters", names = {"-lp", "--local-params"})
        public Map<String, String> params = new HashMap<>();

        /**
         * The flag indicating the use of Simplified Watset.
         */
        @Parameter(description = "Use Simplified Watset", names = {"-s", "--simplified"})
        public boolean simplified = false;
    }

    /**
     * Global clustering command-line interface parameters.
     */
    @SuppressWarnings("unused")
    public static class GlobalParameters {
        /**
         * The global clustering algorithm.
         */
        @Parameter(required = true, description = "Global clustering algorithm", names = {"-g", "--global"})
        public String algorithm;

        /**
         * The global clustering algorithm parameters.
         */
        @DynamicParameter(description = "Global clustering algorithm parameters", names = {"-gp", "--global-params"})
        public Map<String, String> params = new HashMap<>();
    }

    private static final Logger logger = Logger.getLogger(Command.class.getSimpleName());

    /**
     * The main command-line parameters.
     */
    public final MainParameters parameters;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public Command(MainParameters parameters) {
        this.parameters = requireNonNull(parameters, "parameters are not initialized");
    }

    /**
     * Provide a stream to the input file.
     *
     * @return the lines from the file as a {@code Stream}
     * @throws IOException if an I/O error occurs
     */
    public Stream<String> newInputStream() throws IOException {
        requireNonNull(parameters, "parameters are not initialized");

        if (isNull(parameters.input)) {
            logger.info("Reading from standard input.");
            return new BufferedReader(new InputStreamReader(System.in)).lines();
        }

        logger.log(Level.INFO, "Reading from {0}.", parameters.input);
        return Files.lines(parameters.input);
    }

    /**
     * Provide a writer to the output file.
     *
     * @return a new buffered writer to write output
     * @throws IOException if an I/O error occurs
     */
    public BufferedWriter newOutputWriter() throws IOException {
        requireNonNull(parameters, "parameters are not initialized");

        if (isNull(parameters.output)) {
            logger.info("Writing to standard output.");
            return new BufferedWriter(new OutputStreamWriter(System.out));
        }

        logger.log(Level.INFO, "Writing to {0}.", parameters.output);
        return Files.newBufferedWriter(parameters.output);
    }

    /**
     * Read, parse, and return the input graph stored in {@link MainParameters#input}.
     *
     * @return a graph
     * @see ABCFormat#parse(Stream)
     */
    public Graph<String, DefaultWeightedEdge> getGraph() {
        try (final var stream = newInputStream()) {
            final var graph = ABCFormat.parse(stream);

            logger.log(Level.INFO, "Read {0} nodes and {1} edges.",
                    new Object[]{graph.vertexSet().size(), graph.edgeSet().size()});

            return graph;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
