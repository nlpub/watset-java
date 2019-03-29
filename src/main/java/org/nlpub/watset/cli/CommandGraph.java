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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.EmptyClustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.AlgorithmProvider;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.wsi.IndexedSense;
import org.nlpub.watset.wsi.Sense;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Parameters(commandDescription = "Sense Graph")
public class CommandGraph {
    final Application application;

    @Parameter(required = true, description = "Local clustering algorithm", names = {"-l", "--local"})
    private String local;

    @Parameter(description = "Local clustering algorithm parameters", names = {"-lp", "--local-params"})
    private String localParams;

    @Parameter(description = "Use Simplified Watset", names = {"-s", "--simplified"})
    boolean simplified = false;

    public CommandGraph(Application application) {
        this.application = application;
    }

    public void run() {
        final AlgorithmProvider<String, DefaultWeightedEdge> localProvider = new AlgorithmProvider<>(local, localParams);

        final Graph<String, DefaultWeightedEdge> graph = application.getGraph();

        final Graph<Sense<String>, DefaultWeightedEdge> senseGraph = simplified ? getSimplifiedWatsetGraph(graph, localProvider) : getWatsetGraph(graph, localProvider);

        try {
            write(application.output, senseGraph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Path path, Graph<Sense<String>, DefaultWeightedEdge> graph) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (final DefaultWeightedEdge edge : graph.edgeSet()) {
                final IndexedSense<String> source = (IndexedSense<String>) graph.getEdgeSource(edge);
                final IndexedSense<String> target = (IndexedSense<String>) graph.getEdgeTarget(edge);

                writer.write(String.format(Locale.ROOT, "%s#%d\t%s#%d\t%f%n",
                        source.get(), source.getSense(),
                        target.get(), target.getSense(),
                        graph.getEdgeWeight(edge)));
            }
        }
    }

    private Graph<Sense<String>, DefaultWeightedEdge> getWatsetGraph(Graph<String, DefaultWeightedEdge> graph, AlgorithmProvider<String, DefaultWeightedEdge> localProvider) {
        final Watset<String, DefaultWeightedEdge> watset = new Watset<>(graph, localProvider, EmptyClustering.provider(), new CosineContextSimilarity<>());

        watset.fit();

        return watset.getSenseGraph();
    }

    private Graph<Sense<String>, DefaultWeightedEdge> getSimplifiedWatsetGraph(Graph<String, DefaultWeightedEdge> graph, AlgorithmProvider<String, DefaultWeightedEdge> localProvider) {
        final SimplifiedWatset<String, DefaultWeightedEdge> watset = new SimplifiedWatset<>(graph, localProvider, EmptyClustering.provider());

        watset.fit();

        return watset.getSenseGraph();
    }
}
