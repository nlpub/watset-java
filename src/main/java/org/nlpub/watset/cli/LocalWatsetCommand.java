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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.util.ClusteringAlgorithmProvider;

/**
 * A command that uses such local Watset internals as the sense graph and disambiguated contexts.
 *
 * @see SimplifiedWatset
 */
abstract class LocalWatsetCommand extends Command implements WatsetGetter<String, DefaultWeightedEdge> {
    /**
     * The local clustering command-line parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public LocalWatsetCommand(MainParameters parameters) {
        super(parameters);
    }

    /**
     * Get the configured instance of {@link ClusteringAlgorithmProvider}.
     *
     * @return an algorithm provider
     */
    public ClusteringAlgorithmProvider<String, DefaultWeightedEdge> getAlgorithm() {
        return new ClusteringAlgorithmProvider<>(local.algorithm, local.params);
    }
}
