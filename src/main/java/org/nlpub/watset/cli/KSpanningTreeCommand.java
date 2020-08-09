package org.nlpub.watset.cli;

import com.beust.jcommander.Parameter;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

/**
 * A command that runs the <em>k</em> spanning tree clustering algorithm.
 */
class KSpanningTreeCommand extends ClusteringCommand {
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
        return new KSpanningTreeClustering<>(getGraph(), k);
    }
}
