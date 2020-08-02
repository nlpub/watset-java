package org.nlpub.watset.cli;

import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.util.AlgorithmProvider;

/**
 * A command that uses such local Watset internals as the sense graph and disambiguated contexts.
 *
 * @see SimplifiedWatset
 */
abstract class LocalWatsetCommand extends Command implements WatsetGetter<String, DefaultWeightedEdge> {
    /**
     * The local clustering command-line parameters.
     */
    @SuppressWarnings("CanBeFinal")
    @ParametersDelegate
    public LocalParameters local = new LocalParameters();

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public LocalWatsetCommand(MainParameters parameters) {
        super(parameters);
    }

    /**
     * Get the configured instance of {@link AlgorithmProvider}.
     *
     * @return an algorithm provider
     */
    public AlgorithmProvider<String, DefaultWeightedEdge> getAlgorithm() {
        return new AlgorithmProvider<>(local.algorithm, local.params);
    }
}
