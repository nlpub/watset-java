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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.AlgorithmProvider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class CommandMarkovClustering extends ClusteringCommand {
    private final boolean binary;

    @SuppressWarnings("unused")
    @Parameter(names = "-e")
    private Integer e;

    @SuppressWarnings("unused")
    @Parameter(names = "-r")
    private Double r;

    @SuppressWarnings("unused")
    @Parameter(names = "--bin", converter = PathConverter.class)
    private Path binaryPath;

    public CommandMarkovClustering(Application application, boolean binary) {
        super(application);
        this.binary = binary;
    }

    @Override
    public Clustering<String> getClustering() {
        final Map<String, String> params = new HashMap<String, String>() {{
            if (Objects.nonNull(e)) put("e", Integer.toString(e));
            if (Objects.nonNull(r)) put("r", Double.toString(r));
            if (Objects.nonNull(binaryPath)) put("bin", binaryPath.toAbsolutePath().toString());
        }};

        final AlgorithmProvider<String, DefaultWeightedEdge> algorithm = new AlgorithmProvider<>(binary ? "mcl-bin" : "mcl", params);
        return algorithm.apply(application.getGraph());
    }
}
