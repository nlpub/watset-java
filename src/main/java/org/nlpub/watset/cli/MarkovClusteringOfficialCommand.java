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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.AlgorithmProvider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Parameters(commandDescription = "Markov Clustering Official Binary")
public class MarkovClusteringOfficialCommand extends MarkovClusteringCommand {
    @SuppressWarnings("unused")
    @Parameter(description = "Path to binary mcl", names = "--bin", converter = PathConverter.class)
    private Path binary;

    public MarkovClusteringOfficialCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public Clustering<String> getClustering() {
        final Map<String, String> params = new HashMap<>() {{
            if (nonNull(e)) put("e", Integer.toString(e));
            if (nonNull(r)) put("r", Double.toString(r));
            if (nonNull(binary)) put("bin", binary.toAbsolutePath().toString());
        }};

        final var algorithm = new AlgorithmProvider<String, DefaultWeightedEdge>("mcl-bin", params);
        return algorithm.apply(getGraph());
    }
}
