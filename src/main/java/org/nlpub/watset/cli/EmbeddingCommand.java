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
import org.jgrapht.Graphs;
import org.nlpub.watset.util.Matrices;
import org.nlpub.watset.util.Word2VecFormat;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A command that computes graph embeddings.
 */
@Parameters(commandDescription = "Graph Embeddings")
class EmbeddingCommand extends Command {
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
    public EmbeddingCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        final var graph = getGraph();
        final var mapping = Graphs.getVertexToIntegerMapping(graph);
        final var embedding = Matrices.computeSpectralEmbedding(graph, mapping, fixed.k);

        try (final var writer = newOutputWriter()) {
            Word2VecFormat.write(writer, embedding, Word2VecFormat.SpaceStrategy.REPLACE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
