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

/**
 * A command that builds a sense graph with Watset.
 */
@Parameters(commandDescription = "Sense Graph")
class GraphCommand extends LocalWatsetCommand {

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public GraphCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        final var senseGraph = getSenseGraph(getAlgorithm(), getGraph());

        try {
            write(senseGraph);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Graph<Sense<String>, DefaultWeightedEdge> getSenseGraph(ClusteringAlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        final var watset = getWatset(algorithm, EmptyClustering.builder(), graph);
        final var clustering = watset.getClustering();
        return clustering.getSenseGraph();
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
}
