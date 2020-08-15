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

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ClusteringAlgorithmProvider;
import org.nlpub.watset.util.Sense;

import java.util.logging.Logger;

/**
 * A command that runs Watset.
 */
@Parameters(commandDescription = "Watset")
class WatsetCommand extends ClusteringCommand implements WatsetGetter<String, DefaultWeightedEdge> {
    private static final Logger logger = Logger.getLogger(WatsetCommand.class.getSimpleName());

    /**
     * The local clustering command-line parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * The global clustering command-line parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public GlobalParameters global = new GlobalParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public WatsetCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        final var localAlgorithm = new ClusteringAlgorithmProvider<String, DefaultWeightedEdge>(local.algorithm, local.params, parameters.random);
        final var globalAlgorithm = new ClusteringAlgorithmProvider<Sense<String>, DefaultWeightedEdge>(global.algorithm, global.params, parameters.random);
        final var graph = getGraph();

        return getWatset(localAlgorithm, globalAlgorithm, graph);
    }
}
