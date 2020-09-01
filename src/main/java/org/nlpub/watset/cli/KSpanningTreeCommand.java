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
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

/**
 * A command that runs the <em>k</em> spanning tree clustering algorithm.
 */
@Parameters(commandDescription = "k Spanning Tree Clustering")
class KSpanningTreeCommand extends ClusteringCommand {
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
     */
    public KSpanningTreeCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        return new KSpanningTreeClustering<>(getGraph(), fixed.k);
    }
}
