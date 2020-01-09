package org.nlpub.watset.cli;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.util.AlgorithmProvider;

import java.util.Collections;

class CommandTrivial extends ClusteringCommand {
    private final String algorithm;

    public CommandTrivial(Application application, String algorithm) {
        super(application);
        this.algorithm = algorithm;
    }

    @Override
    public Clustering<String> getClustering() {
        return new AlgorithmProvider<String, DefaultWeightedEdge>(algorithm, Collections.emptyMap()).
                apply(application.getGraph());
    }
}
