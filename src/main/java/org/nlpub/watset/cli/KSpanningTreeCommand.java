package org.nlpub.watset.cli;

import com.beust.jcommander.Parameter;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.ClusteringAlgorithmProvider;

import java.util.Map;

/**
 * A command that runs the <em>k</em> spanning tree clustering algorithm.
 */
public class KSpanningTreeCommand extends ClusteringCommand {
    @SuppressWarnings("unused")
    @Parameter(description = "Desired number of clusters", names = "-k")
    private Integer k;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public KSpanningTreeCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public ClusteringAlgorithm<String> getAlgorithm() {
        final var params = Map.of("k", String.valueOf(k));
        return new ClusteringAlgorithmProvider<String, DefaultWeightedEdge>("kst", params).apply(getGraph());
    }
}
