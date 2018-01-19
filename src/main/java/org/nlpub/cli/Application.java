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

package org.nlpub.cli;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.graph.Clustering;
import org.nlpub.io.ABCParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class Application {
    private static final IDefaultProvider DEFAULT_PROVIDER = option -> {
        switch (option.toLowerCase()) {
            case "--input":
                return "/dev/stdin";
            case "--output":
                return "/dev/stdout";
            default:
                return null;
        }
    };

    @Parameter(names = {"-i", "--input"}, description = "Input file", converter = PathConverter.class)
    public Path input;

    @Parameter(names = {"-o", "--output"}, description = "Output file", converter = PathConverter.class)
    public Path output;

    public Graph<String, DefaultWeightedEdge> getGraph() {
        try {
            return parse(input, ABCParser::parse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // TODO: Use the main argument for --input instead of the named one.
        final Application application = new Application();

        final CommandChineseWhispers cw = new CommandChineseWhispers(application);
        final CommandMarkovClustering mcl = new CommandMarkovClustering(application);
        final CommandSenses senses = new CommandSenses(application);
        final CommandWatset watset = new CommandWatset(application);
        final CommandMaxMax maxmax = new CommandMaxMax(application);

        final JCommander jc = JCommander.newBuilder()
                .addObject(application)
                .addCommand("cw", cw)
                .addCommand("mcl", mcl)
                .addCommand("senses", senses)
                .addCommand("watset", watset)
                .addCommand("maxmax", maxmax)
                .defaultProvider(DEFAULT_PROVIDER)
                .build();

        jc.parse(args);

        if (Objects.isNull(jc.getParsedCommand())) {
            System.err.println("Please read the documentation.");
            System.exit(1);
        }

        switch (jc.getParsedCommand().toLowerCase()) {
            case "cw":
                cw.run();
                break;
            case "mcl":
                mcl.run();
                break;
            case "senses":
                senses.run();
                break;
            case "watset":
                watset.run();
                break;
            case "maxmax":
                maxmax.run();
                break;
            default:
                break;
        }
    }

    public static <T> T parse(Path path, Function<Stream<String>, T> f) throws IOException {
        try (final Stream<String> stream = Files.lines(path)) {
            return f.apply(stream);
        }
    }

    public static void write(Path path, Clustering<String> clustering) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            int i = 0;
            for (final Collection<String> cluster : clustering.getClusters()) {
                writer.write(String.format(Locale.ROOT, "%d\t%d\t%s\n", i++, cluster.size(), String.join(", ", cluster)));
            }
        }
    }
}
