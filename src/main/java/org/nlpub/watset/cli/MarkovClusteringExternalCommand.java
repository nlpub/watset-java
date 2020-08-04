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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.MarkovClusteringExternal;

import java.nio.file.Path;

import static java.util.Objects.nonNull;

/**
 * A command that runs the official binary for Markov Clustering.
 */
@SuppressWarnings("unused")
@Parameters(commandDescription = "Markov Clustering Official Binary")
class MarkovClusteringExternalCommand extends ClusteringCommand {
    @Parameter(description = "Inflation parameter", names = "-r")
    private Double r;

    @Parameter(description = "Path to binary mcl", names = "--bin", converter = PathConverter.class)
    private Path binary;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public MarkovClusteringExternalCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        final var builder = new MarkovClusteringExternal.Builder<String, DefaultWeightedEdge>();

        if (nonNull(binary)) builder.setPath(binary);
        if (nonNull(r)) builder.setR(r);

        return builder.build(getGraph());
    }
}
