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

package io.github.dustalov.maxmax;

import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

abstract class Application {
    public static void main(String[] args) throws IOException {
        final CommandLineParser parser = new DefaultParser();

        final Options options = new Options();
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
        final MaxMax<String> maxmax = new MaxMax<>(graph);
        maxmax.run();
        write(cmd.getOptionValue("out"), maxmax);
    }

    private static <T> T parse(String filename, Function<Stream<String>, T> f) throws IOException {
        try (final Stream<String> stream = Files.lines(Paths.get(filename))) {
            return f.apply(stream);
        }
    }

    private static void write(String filename, MaxMax<String> maxmax) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            int i = 0;
            for (final Set<String> cluster : maxmax.getClusters()) {
                writer.write(String.format(Locale.ROOT, "%d\t%d\t%s\n", i++, cluster.size(), String.join(", ", cluster)));
            }
        }
    }
}
