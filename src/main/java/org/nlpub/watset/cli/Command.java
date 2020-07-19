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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ABCFormat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

/**
 * A generic command of the Watset command-line interface.
 */
public abstract class Command implements Runnable {
    /**
     * Watset command-line interface parameters.
     */
    public static class Parameters {

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

    private static final Logger logger = Logger.getLogger(Command.class.getSimpleName());

    /**
     * The command-line parameters.
     */
    @ParametersDelegate
    public Parameters parameters = new Parameters();

    /**
     * Provide a stream to the input file.
     *
     * @return the lines from the file as a {@code Stream}
     * @throws IOException if an I/O error occurs
     */
    public Stream<String> newInputStream() throws IOException {
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
        if (isNull(parameters.output)) {
            logger.info("Writing to standard output.");
            return new BufferedWriter(new OutputStreamWriter(System.out));
        }

        logger.log(Level.INFO, "Writing to {0}.", parameters.output);
        return Files.newBufferedWriter(parameters.output);
    }

    /**
     * Read, parse, and return the input graph stored in {@link Parameters#input}.
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
