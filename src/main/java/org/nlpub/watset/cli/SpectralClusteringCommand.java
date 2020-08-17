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

import com.beust.jcommander.ParametersDelegate;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.NodeEmbedding;
import org.nlpub.watset.graph.SpectralClustering;

/**
 * A command that runs the spectral clustering algorithm.
 */
class SpectralClusteringCommand extends ClusteringCommand {
    /**
     * The number of clusters parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public Command.FixedClustersParameters fixed = new Command.FixedClustersParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public SpectralClusteringCommand(Command.MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        final var clusterer = new KMeansPlusPlusClusterer<NodeEmbedding<String>>(fixed.k, -1, new EuclideanDistance(), parameters.random);
        return SpectralClustering.<String, DefaultWeightedEdge>builder().setClusterer(clusterer).setK(fixed.k).apply(getGraph());
    }
}
