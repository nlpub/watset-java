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

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ABCFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * Watset command-line interface.
 */
public class Application {
    private static final Logger logger = Logger.getLogger(Application.class.getSimpleName());

    private static final IDefaultProvider DEFAULT_PROVIDER = option -> {
        switch (option.toLowerCase(Locale.ROOT)) {
            case "--input":
                return "/dev/stdin";
            case "--output":
                return "/dev/stdout";
            default:
                return null;
        }
    };

    /**
     * The input file.
     */
    @SuppressWarnings("unused")
    @Parameter(names = {"-i", "--input"}, description = "Input file", converter = PathConverter.class)
    public Path input;

    /**
     * The output file.
     */
    @SuppressWarnings("unused")
    @Parameter(names = {"-o", "--output"}, description = "Output file", converter = PathConverter.class)
    public Path output;

    /**
     * Read, parse, and return the input graph stored in {@link #input}.
     *
     * @return a graph
     * @see ABCFormat#parse(Stream)
     */
    public Graph<String, DefaultWeightedEdge> getGraph() {
        try (final var stream = Files.lines(input)) {
            final var graph = ABCFormat.parse(stream);
            logger.log(Level.INFO, "Read {0} nodes and {1} edges from {2}.",
                    new Object[]{graph.vertexSet().size(), graph.edgeSet().size(), input.toAbsolutePath()});
            return graph;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Watset Command-Line Interface Entry Point.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // TODO: Use the main argument for --input instead of the named one.
        final var application = new Application();

        final var empty = new CommandTrivial(application, "empty");
        final var singleton = new CommandTrivial(application, "singleton");
        final var together = new CommandTrivial(application, "together");
        final var components = new CommandTrivial(application, "components");
        final var cw = new CommandChineseWhispers(application);
        final var mcl = new CommandMarkovClustering(application, false);
        final var mclBin = new CommandMarkovClustering(application, true);
        final var senses = new CommandSenses(application);
        final var graph = new CommandGraph(application);
        final var watset = new CommandWatset(application);
        final var maxmax = new CommandMaxMax(application);

        final var jc = JCommander.newBuilder()
                .addObject(application)
                .addCommand("empty", empty)
                .addCommand("singleton", singleton)
                .addCommand("together", together)
                .addCommand("components", components)
                .addCommand("cw", cw)
                .addCommand("mcl", mcl)
                .addCommand("mcl-bin", mclBin)
                .addCommand("senses", senses)
                .addCommand("graph", graph)
                .addCommand("watset", watset)
                .addCommand("maxmax", maxmax)
                .defaultProvider(DEFAULT_PROVIDER)
                .build();

        jc.parse(args);

        if (isNull(jc.getParsedCommand())) {
            System.err.println("Please read the documentation.");
            System.exit(1);
        }

        var objects = requireNonNull(jc.getCommands().get(jc.getParsedCommand()).getObjects());
        var command = (Runnable) objects.get(0);
        command.run();
    }
}
