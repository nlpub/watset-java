package io.github.dustalov.maxmax;

import org.apache.commons.cli.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

abstract class Application {
    public static void main(String[] args) throws IOException {
        final CommandLineParser parser = new DefaultParser();

        final Options options = new Options();
        options.addOption(Option.builder("in").argName("in").desc("input graph in ABC format (uncompressed or gzipped)").hasArg().required().build());
        options.addOption(Option.builder("out").argName("out").desc("name of cluster output file (add .gz for compressed output)").hasArg().required().build());

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException ex) {
            System.err.println(ex.getMessage());
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar this.jar", options, true);
            System.exit(1);
        }

        final UndirectedGraph<String, DefaultWeightedEdge> graph = parse(cmd.getOptionValue("in"), ABCParser::parse);
        final MaxMax<String> maxmax = new MaxMax<>(graph);
        maxmax.run();
        write(cmd.getOptionValue("out"), maxmax);
    }

    private static <T> T parse(String filename, Function<Stream<String>, T> f) throws IOException {
        try (final Stream<String> stream = Files.lines(Paths.get(filename))) {
            return f.apply(stream);
        }
    }

    private static <T> void write(String filename, MaxMax<String> maxmax) throws IOException {
        final AtomicInteger index = new AtomicInteger();
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            maxmax.getClusters().forEach(cluster -> {
                final String line = new StringBuilder().
                        append(index.getAndIncrement()).
                        append('\t').
                        append(cluster.size()).
                        append('\t').
                        append(String.join(",", cluster)).
                        append('\n').
                        toString();
                try {
                    writer.write(line);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
}
