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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Parameters(commandDescription = "Sense Graph")
class CommandGraph implements Runnable {
    final Application application;

    @SuppressWarnings("unused")
    @Parameter(required = true, description = "Local clustering algorithm", names = {"-l", "--local"})
    private String local;

    @SuppressWarnings({"FieldMayBeFinal"})
    @DynamicParameter(description = "Local clustering algorithm parameters", names = {"-lp", "--local-params"})
    private Map<String, String> localParams = new HashMap<>();

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Parameter(description = "Use Simplified Watset", names = {"-s", "--simplified"})
    private boolean simplified = false;

    public CommandGraph(Application application) {
        this.application = application;
    }

    public void run() {
        final var localProvider = new AlgorithmProvider<String, DefaultWeightedEdge>(local, localParams);

        final var graph = application.getGraph();

        final var senseGraph = simplified ? getSimplifiedWatsetGraph(graph, localProvider) : getWatsetGraph(graph, localProvider);

        try {
            write(application.output, senseGraph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Path path, Graph<Sense<String>, DefaultWeightedEdge> graph) throws IOException {
        try (final var writer = Files.newBufferedWriter(path)) {
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
