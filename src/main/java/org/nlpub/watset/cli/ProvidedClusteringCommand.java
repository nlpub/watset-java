/*
 * Copyright 2020 Dmitry Ustalov
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
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.AlgorithmProvider;

/**
 * A command that uses {@link AlgorithmProvider} to resolve the clustering algorithm.
 */
@Parameters(commandDescription = "Clustering")
class ProvidedClusteringCommand extends ClusteringCommand {
    private final String algorithm;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     * @param algorithm  the algorithm
     */
    public ProvidedClusteringCommand(MainParameters parameters, String algorithm) {
        super(parameters);
        this.algorithm = algorithm;
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        return new AlgorithmProvider<String, DefaultWeightedEdge>(algorithm).apply(getGraph());
    }
}
