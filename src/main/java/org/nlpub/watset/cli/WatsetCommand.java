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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.AlgorithmProvider;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.util.Sense;

@Parameters(commandDescription = "Watset")
class WatsetCommand extends ClusteringCommand {
    /**
     * The local clustering command-line parameters.
     */
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * The global clustering command-line parameters.
     */
    @ParametersDelegate
    public GlobalParameters global = new GlobalParameters();

    @Override
    public Clustering<String> getClustering() {
        final var localAlgorithm = new AlgorithmProvider<String, DefaultWeightedEdge>(local.algorithm, local.params);
        final var globalAlgorithm = new AlgorithmProvider<Sense<String>, DefaultWeightedEdge>(global.algorithm, global.params);

        final var graph = getGraph();

        if (local.simplified) {
            return new SimplifiedWatset<>(graph, localAlgorithm, globalAlgorithm);
        } else {
            @SuppressWarnings("deprecation") final var watset = new Watset<>(graph, localAlgorithm, globalAlgorithm, new CosineContextSimilarity<>());
            return watset;
        }
    }
}
