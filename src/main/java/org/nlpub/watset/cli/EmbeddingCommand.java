package org.nlpub.watset.cli;

import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.Graphs;
import org.nlpub.watset.util.Matrices;
import org.nlpub.watset.util.Word2VecFormat;

import java.io.IOException;
import java.io.UncheckedIOException;

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
