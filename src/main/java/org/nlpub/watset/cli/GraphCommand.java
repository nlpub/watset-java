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
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.io.IOException;
import java.util.Locale;

@Parameters(commandDescription = "Sense Graph")
class GraphCommand extends Command {
    /**
     * The local clustering command-line parameters.
     */
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public GraphCommand(MainParameters parameters) {
        super(parameters);
    }

    public void run() {
        final var algorithm = new AlgorithmProvider<String, DefaultWeightedEdge>(local.algorithm, local.params);

        final var graph = getGraph();

        final var senseGraph = local.simplified ?
                getSimplifiedWatsetGraph(graph, algorithm) :
                getWatsetGraph(graph, algorithm);

        try {
            write(senseGraph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Graph<Sense<String>, DefaultWeightedEdge> graph) throws IOException {
        try (final var writer = newOutputWriter()) {
            for (final var edge : graph.edgeSet()) {
                final var source = (IndexedSense<String>) graph.getEdgeSource(edge);
                final var target = (IndexedSense<String>) graph.getEdgeTarget(edge);

                writer.write(String.format(Locale.ROOT, "%s#%d\t%s#%d\t%f%n",
                        source.get(), source.getSense(),
                        target.get(), target.getSense(),
                        graph.getEdgeWeight(edge)));
            }
        }
    }

    private Graph<Sense<String>, DefaultWeightedEdge> getWatsetGraph(Graph<String, DefaultWeightedEdge> graph, AlgorithmProvider<String, DefaultWeightedEdge> localProvider) {
        @SuppressWarnings("deprecation") final var watset = new Watset<>(graph, localProvider, EmptyClustering.provider(), new CosineContextSimilarity<>());

        watset.fit();

        return watset.getSenseGraph();
    }

    private Graph<Sense<String>, DefaultWeightedEdge> getSimplifiedWatsetGraph(Graph<String, DefaultWeightedEdge> graph, AlgorithmProvider<String, DefaultWeightedEdge> localProvider) {
        final var watset = new SimplifiedWatset<>(graph, localProvider, EmptyClustering.provider());

        watset.fit();

        return watset.getSenseGraph();
    }
}
