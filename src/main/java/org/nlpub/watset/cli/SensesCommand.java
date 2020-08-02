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
import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.EmptyClustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.AlgorithmProvider;
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.joining;

@Parameters(commandDescription = "Sense Induction")
class SensesCommand extends Command {
    /**
     * The local clustering command-line parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public SensesCommand(MainParameters parameters) {
        super(parameters);
    }

    public void run() {
        final var algorithm = new AlgorithmProvider<String, DefaultWeightedEdge>(local.algorithm, local.params);

        final var graph = getGraph();

        final var contexts = local.simplified ?
                getSimplifiedWatsetContexts(algorithm, graph) :
                getWatsetContexts(algorithm, graph);

        try {
            write(contexts);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void write(Map<Sense<String>, Map<Sense<String>, Number>> contexts) throws IOException {
        try (final var writer = newOutputWriter()) {
            for (final var context : contexts.entrySet()) {
                final var sense = ((IndexedSense<String>) context.getKey());

                final var contextRecord = context.getValue().entrySet().stream().
                        map(e -> String.format(Locale.ROOT, "%s#%d:%f",
                                e.getKey().get(),
                                ((IndexedSense<String>) e.getKey()).getSense(),
                                e.getValue().doubleValue())).
                        collect(joining(","));

                writer.write(String.format(Locale.ROOT, "%s\t%d\t%s%n", sense.get(), sense.getSense(), contextRecord));
            }
        }
    }

    public Map<Sense<String>, Map<Sense<String>, Number>> getWatsetContexts(AlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        @SuppressWarnings("deprecation") final var watset = new Watset.Builder<String, DefaultWeightedEdge>().
                setLocal(algorithm).
                setGlobal(EmptyClustering.provider()).
                build(graph);

        watset.fit();

        return watset.getContexts();
    }

    public Map<Sense<String>, Map<Sense<String>, Number>> getSimplifiedWatsetContexts(AlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        final var watset = new SimplifiedWatset.Builder<String, DefaultWeightedEdge>().
                setLocal(algorithm).
                setGlobal(EmptyClustering.provider()).
                build(graph);

        watset.fit();

        return watset.getContexts();
    }
}
