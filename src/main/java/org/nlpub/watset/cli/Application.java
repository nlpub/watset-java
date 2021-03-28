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

import com.beust.jcommander.JCommander;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * Watset command-line interface.
 */
public final class Application implements Runnable {
    /**
     * Status of command-line argument parsing.
     */
    public enum ParseStatus {
        /**
         * A command has been parsed successfully.
         */
        COMMAND,

        /**
         * No command has been found.
         */
        EMPTY,

        /**
         * No command has been found, but the version has been requested.
         */
        EMPTY_BUT_VERSION
    }

    /**
     * The command-line argument parser.
     */
    private final JCommander jc;

    /**
     * The parsing status.
     */
    private ParseStatus status = ParseStatus.EMPTY;

    /**
     * Watset command-line interface entry point.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        final var app = new Application();

        switch (app.parse(args)) {
            case COMMAND:
            case EMPTY_BUT_VERSION:
                app.run();
                break;
            default:
                app.jc.usage();
                System.exit(1);
                break;
        }
    }

    /**
     * Create an instance of the Watset command-line interface.
     */
    public Application() {
        final var parameters = new Command.MainParameters();

        // TODO: Use the main argument for --input instead of the named one.
        jc = JCommander.newBuilder()
                .addObject(parameters)
                .addCommand("empty", new ProvidedClusteringCommand(parameters, "empty"))
                .addCommand("singleton", new ProvidedClusteringCommand(parameters, "singleton"))
                .addCommand("together", new ProvidedClusteringCommand(parameters, "together"))
                .addCommand("components", new ProvidedClusteringCommand(parameters, "components"))
                .addCommand("gn", new ProvidedClusteringFixedKCommand(parameters, "gn"))
                .addCommand("kst", new ProvidedClusteringFixedKCommand(parameters, "kst"))
                .addCommand("spectral", new SpectralClusteringCommand(parameters))
                .addCommand("cw", new ChineseWhispersCommand(parameters))
                .addCommand("mcl", new MarkovClusteringCommand(parameters))
                .addCommand("mcl-bin", new MarkovClusteringExternalCommand(parameters))
                .addCommand("embed", new EmbeddingCommand(parameters))
                .addCommand("senses", new SensesCommand(parameters))
                .addCommand("graph", new GraphCommand(parameters))
                .addCommand("embedsenses", new SenseEmbeddingCommand(parameters))
                .addCommand("watset", new WatsetCommand(parameters))
                .addCommand("maxmax", new ProvidedClusteringCommand(parameters, "maxmax"))
                .addCommand("pairwise", new PairwiseCommand(parameters))
                .addCommand("purity", new PurityCommand(parameters))
                .addCommand("version", new VersionCommand(parameters))
                .build();
    }

    /**
     * Parse the command-line arguments.
     *
     * @param args the command-line arguments
     * @return the status
     */
    public ParseStatus parse(String... args) {
        status = ParseStatus.EMPTY;

        jc.parse(args);

        final var parameters = (Command.MainParameters) jc.getObjects().get(0);

        if (nonNull(jc.getParsedCommand())) {
            status = ParseStatus.COMMAND;
        } else if (parameters.version) {
            status = ParseStatus.EMPTY_BUT_VERSION;
        }

        return status;
    }

    /**
     * Run the parsed command.
     */
    @Override
    public void run() {
        final var command = status == ParseStatus.EMPTY_BUT_VERSION ?
                "version" :
                requireNonNull(jc.getParsedCommand(), "command should not be null");

        run(command);
    }

    /**
     * Run the specified command.
     *
     * @param command the command
     */
    public void run(String command) {
        final var objects = jc.getCommands().get(command).getObjects();
        final var runnable = (Command) objects.get(0);
        runnable.run();
    }
}
