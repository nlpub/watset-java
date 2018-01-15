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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.graph.Clustering;
import org.nlpub.vsm.ContextCosineSimilarity;
import org.nlpub.watset.Application;
import org.nlpub.watset.Watset;
import org.nlpub.watset.sense.Sense;

@Parameters(commandDescription = "Watset")
public class CommandWatset extends ClusteringCommand {
    @Parameter(required = true, description = "Local clustering algorithm", names = {"-l", "--local"})
    private String local;

    @Parameter(description = "Local clustering algorithm parameters", names = {"-lp", "--local-params"})
    private String localParams;

    @Parameter(required = true, description = "Global clustering algorithm", names = {"-g", "--global"})
    private String global;

    @Parameter(description = "Global clustering algorithm parameters", names = {"-gp", "--global-params"})
    private String globalParams;

    public CommandWatset(Application application) {
        super(application);
    }

    @Override
    public Clustering<String> getClustering() {
        final AlgorithmProvider<String, DefaultWeightedEdge> localAlgorithm = new AlgorithmProvider<>(local, localParams);
        final AlgorithmProvider<Sense<String>, DefaultWeightedEdge> globalAlgorithm = new AlgorithmProvider<>(global, globalParams);

        final Graph<String, DefaultWeightedEdge> graph = application.getGraph();

        return new Watset<>(
                graph, localAlgorithm, globalAlgorithm,
                new ContextCosineSimilarity<>()
        );
    }
}
