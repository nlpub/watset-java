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

/**
 * A command that runs Markov Clustering.
 */
@SuppressWarnings("unused")
@Parameters(commandDescription = "Markov Clustering")
class MarkovClusteringCommand extends ClusteringCommand {
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Parameter(description = "Expansion parameter", names = "-e")
    private int e = MarkovClustering.Builder.E;

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Parameter(description = "Inflation parameter", names = "-r")
    private double r = MarkovClustering.Builder.R;

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Parameter(description = "Number of iterations", names = {"-i", "--iterations"})
    private int iterations = MarkovClustering.Builder.ITERATIONS;

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
        return MarkovClustering.<String, DefaultWeightedEdge>builder().
                setE(e).
                setR(r).
                setIterations(iterations).
                build(getGraph());
    }
}
