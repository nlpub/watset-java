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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.AlgorithmProvider;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.util.Sense;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Watset")
class WatsetCommand extends ClusteringCommand {
    @SuppressWarnings("unused")
    @Parameter(required = true, description = "Local clustering algorithm", names = {"-l", "--local"})
    private String local;

    @SuppressWarnings({"FieldMayBeFinal"})
    @DynamicParameter(description = "Local clustering algorithm parameters", names = {"-lp", "--local-params"})
    private Map<String, String> localParams = new HashMap<>();

    @SuppressWarnings("unused")
    @Parameter(required = true, description = "Global clustering algorithm", names = {"-g", "--global"})
    private String global;

    @SuppressWarnings({"FieldMayBeFinal"})
    @DynamicParameter(description = "Global clustering algorithm parameters", names = {"-gp", "--global-params"})
    private Map<String, String> globalParams = new HashMap<>();

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Parameter(description = "Use Simplified Watset", names = {"-s", "--simplified"})
    private boolean simplified = false;

    @Override
    public Clustering<String> getClustering() {
        final var localProvider = new AlgorithmProvider<String, DefaultWeightedEdge>(local, localParams);
        final var globalProvider = new AlgorithmProvider<Sense<String>, DefaultWeightedEdge>(global, globalParams);

        final var graph = getGraph();

        if (simplified) {
            return new SimplifiedWatset<>(graph, localProvider, globalProvider);
        } else {
            @SuppressWarnings("deprecation") final var watset = new Watset<>(graph, localProvider, globalProvider, new CosineContextSimilarity<>());
            return watset;
        }
    }
}
