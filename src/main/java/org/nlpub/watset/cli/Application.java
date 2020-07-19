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

import static java.util.Objects.isNull;

/**
 * Watset command-line interface.
 */
public class Application {
    /**
     * Watset Command-Line Interface Entry Point.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // TODO: Use the main argument for --input instead of the named one.
        final var parameters = new Command.Parameters();

        final var jc = JCommander.newBuilder()
                .addObject(parameters)
                .addCommand("empty", new TrivialCommand("empty"))
                .addCommand("singleton", new TrivialCommand("singleton"))
                .addCommand("together", new TrivialCommand("together"))
                .addCommand("components", new TrivialCommand("components"))
                .addCommand("cw", new ChineseWhispersCommand())
                .addCommand("mcl", new MarkovClusteringCommand(false))
                .addCommand("mcl-bin", new MarkovClusteringCommand(true))
                .addCommand("senses", new SensesCommand())
                .addCommand("graph", new GraphCommand())
                .addCommand("watset", new WatsetCommand())
                .addCommand("maxmax", new MaxMaxCommand())
                .build();

        jc.parse(args);

        if (isNull(jc.getParsedCommand())) {
            System.err.println("Please read the documentation.");
            System.exit(1);
        }

        var objects = jc.getCommands().get(jc.getParsedCommand()).getObjects();

        var command = (Command) objects.get(0);
        command.run();
    }
}
