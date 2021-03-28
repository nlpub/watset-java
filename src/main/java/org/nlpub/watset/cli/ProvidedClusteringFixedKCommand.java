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
import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ClusteringAlgorithmProvider;

import java.util.Map;

/**
 * A command that uses {@link ClusteringAlgorithmProvider} to resolve the clustering algorithm
 * with the pre-defined number of clusters <em>k</em>.
 */
@Parameters(commandDescription = "Clustering with Fixed Number of Clusters")
class ProvidedClusteringFixedKCommand extends ProvidedClusteringCommand {
    /**
     * The number of clusters parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public FixedClustersParameters fixed = new FixedClustersParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     * @param algorithm  the algorithm
     */
    public ProvidedClusteringFixedKCommand(MainParameters parameters, String algorithm) {
        super(parameters, algorithm);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        return new ClusteringAlgorithmProvider<String, DefaultWeightedEdge>(
                algorithm,
                Map.of("k", String.valueOf(fixed.k)),
                parameters.random
        ).apply(getGraph());
    }
}
