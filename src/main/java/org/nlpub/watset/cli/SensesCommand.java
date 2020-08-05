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

package org.nlpub.watset.cli;

import com.beust.jcommander.Parameters;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.EmptyClustering;
import org.nlpub.watset.util.ClusteringAlgorithmProvider;
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A command that induces node senses with Watset.
 */
@Parameters(commandDescription = "Sense Induction")
class SensesCommand extends LocalWatsetCommand {
    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public SensesCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        final var contexts = getContexts(getAlgorithm(), getGraph());

        try {
            write(contexts);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<Sense<String>, Map<Sense<String>, Number>> getContexts(ClusteringAlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        if (local.simplified) {
            final var watset = getSimplifiedWatset(algorithm, EmptyClustering.provider(), graph);
            final var clustering = watset.getClustering();
            return clustering.getContexts();
        } else {
            final var watset = getWatset(algorithm, EmptyClustering.provider(), graph);
            final var clustering = watset.getClustering();
            return clustering.getContexts();
        }
    }

    private void write(Map<Sense<String>, Map<Sense<String>, Number>> contexts) throws IOException {
        try (final var writer = newOutputWriter()) {
            for (final var context : contexts.entrySet()) {
                final var sense = ((IndexedSense<String>) context.getKey());

                final var contextRecord = context.getValue().entrySet().stream().
                        map(e -> String.format(Locale.ROOT, "%s#%d:%f",
                                e.getKey().get(),
                                ((IndexedSense<String>) e.getKey()).getSense(),
                                e.getValue().doubleValue())).
                        collect(Collectors.joining(","));

                writer.write(String.format(Locale.ROOT, "%s\t%d\t%s%n", sense.get(), sense.getSense(), contextRecord));
            }
        }
    }
}
