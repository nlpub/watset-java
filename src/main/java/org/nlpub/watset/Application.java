/*
 * Copyright 2016 Dmitry Ustalov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.nlpub.watset;

import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.cw.ChineseWhispers;
import org.nlpub.cw.weighting.*;
import org.nlpub.graph.Clustering;
import org.nlpub.io.ABCParser;
import org.nlpub.maxmax.MaxMax;
import org.nlpub.mcl.MarkovClustering;
import org.nlpub.vsm.ContextCosineSimilarity;
import org.nlpub.watset.sense.Sense;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

abstract class Application {
    public static void main(String[] args) throws IOException {
        final CommandLineParser parser = new DefaultParser();

        final Options options = new Options();
        options.addOption(Option.builder("method").argName("method").desc("clustering algorithm").hasArg().required().build());
        options.addOption(Option.builder("watset_cw").argName("watset_cw").desc("variation of the local Chinese Whispers in Watset: chris|top|nolog|log (Watset only)").hasArg().build());
        options.addOption(Option.builder("cw").argName("cw").desc("variation of the global Chinese Whispers: chris|top|nolog|log (Watset and CW only)").hasArg().build());
        options.addOption(Option.builder("in").argName("in").desc("input graph in the ABC format").hasArg().required().build());
        options.addOption(Option.builder("out").argName("out").desc("name of cluster output file").hasArg().required().build());

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException ex) {
            System.err.println(ex.getMessage());
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar this.jar", options, true);
            System.exit(1);
        }

        final Graph<String, DefaultWeightedEdge> graph = parse(cmd.getOptionValue("in"), ABCParser::parse);

        Clustering<String> algorithm = null;

        final String globalNodeWeightingOption = cmd.getOptionValue("cw");

        if (cmd.getOptionValue("method").equalsIgnoreCase("maxmax")) {
            algorithm = new MaxMax<>(graph);
        } else if (cmd.getOptionValue("method").equalsIgnoreCase("mcl")) {
            final Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> globalClusteringProvider =
                    global -> new MarkovClustering<>(global, 2, 2);

            algorithm = globalClusteringProvider.apply(graph);
        } else if (cmd.getOptionValue("method").equalsIgnoreCase("cw")) {
            final Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> globalClusteringProvider = global -> {
                final NodeSelector<String, DefaultWeightedEdge> globalNodeSelector = parseNodeWeighting(globalNodeWeightingOption);
                return new ChineseWhispers<>(global, globalNodeSelector);
            };

            algorithm = globalClusteringProvider.apply(graph);
        } else if (cmd.getOptionValue("method").equalsIgnoreCase("watset")) {
            final String localNodeWeightingOption = cmd.getOptionValue("watset_cw");

            final Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> localClusteringProvider = ego -> {
                final NodeSelector<String, DefaultWeightedEdge> localNodeSelector = parseNodeWeighting(localNodeWeightingOption);
                return new ChineseWhispers<>(ego, localNodeSelector);
            };

            final Function<Graph<Sense<String>, DefaultWeightedEdge>, Clustering<Sense<String>>> globalClusteringProvider = global -> {
                final NodeSelector<Sense<String>, DefaultWeightedEdge> globalNodeSelector = parseNodeWeighting(globalNodeWeightingOption);
                return new ChineseWhispers<>(global, globalNodeSelector);
            };

            algorithm = new Watset<>(graph, localClusteringProvider, globalClusteringProvider, new ContextCosineSimilarity<>());
        } else {
            System.err.println("No method selected.");
            System.exit(1);
        }

        algorithm.run();
        write(cmd.getOptionValue("out"), algorithm);
    }

    private static <T> T parse(String filename, Function<Stream<String>, T> f) throws IOException {
        try (final Stream<String> stream = Files.lines(Paths.get(filename))) {
            return f.apply(stream);
        }
    }

    private static void write(String filename, Clustering<String> maxmax) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            int i = 0;
            for (final Collection<String> cluster : maxmax.getClusters()) {
                writer.write(String.format(Locale.ROOT, "%d\t%d\t%s\n", i++, cluster.size(), String.join(", ", cluster)));
            }
        }
    }

    private static <V> NodeSelector<V, DefaultWeightedEdge> parseNodeWeighting(String value) {
        if (Objects.nonNull(value)) {
            if (value.equalsIgnoreCase("top")) {
                return new TopWeighting<>();
            } else if (value.equalsIgnoreCase("nolog")) {
                return new ProportionalWeighting<>();
            } else if (value.equalsIgnoreCase("log")) {
                return new LogWeighting<>();
            }
        }

        return new ChrisWeighting<>();
    }
}
