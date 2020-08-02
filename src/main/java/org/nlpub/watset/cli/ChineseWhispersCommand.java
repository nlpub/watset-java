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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.AlgorithmProvider;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * A command that runs Chinese Whispers.
 */
@Parameters(commandDescription = "Chinese Whispers")
class ChineseWhispersCommand extends ClusteringCommand {
    @SuppressWarnings("unused")
    @Parameter(description = "Node weighting mode (top, log, lin)", names = {"-m", "--mode"})
    private String mode;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public ChineseWhispersCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public Clustering<String> getClustering() {
        final Map<String, String> params = nonNull(mode) ? Map.of("mode", mode) : Collections.emptyMap();

        final var algorithm = new AlgorithmProvider<String, DefaultWeightedEdge>("cw", params);

        return algorithm.apply(getGraph());
    }
}
