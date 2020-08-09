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
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.MarkovClustering;

import static java.util.Objects.nonNull;

/**
 * A command that runs Markov Clustering.
 */
@SuppressWarnings("unused")
@Parameters(commandDescription = "Markov Clustering")
class MarkovClusteringCommand extends ClusteringCommand {
    @Parameter(description = "Expansion parameter", names = "-e")
    private Integer e;

    @Parameter(description = "Inflation parameter", names = "-r")
    private Double r;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public MarkovClusteringCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        final var builder = MarkovClustering.<String, DefaultWeightedEdge>builder();

        if (nonNull(e)) builder.setE(e);
        if (nonNull(r)) builder.setR(r);

        return builder.build(getGraph());
    }
}
