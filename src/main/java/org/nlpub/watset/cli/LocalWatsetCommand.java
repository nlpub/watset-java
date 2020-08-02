package org.nlpub.watset.cli;

import com.beust.jcommander.ParametersDelegate;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.EmptyClustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.AlgorithmProvider;

/**
 * A command that uses such local Watset internals as the sense graph and disambiguated contexts.
 *
 * @see SimplifiedWatset
 */
abstract class LocalWatsetCommand extends Command {
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

    /**
     * Construct an instance of {@link SimplifiedWatset}.
     *
     * @param algorithm the local clustering algorithm provider
     * @param graph     the graph
     * @return an instance of Simplified Watset
     */
    public SimplifiedWatset<String, DefaultWeightedEdge> getSimplifiedWatset(AlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        return new SimplifiedWatset.Builder<String, DefaultWeightedEdge>().
                setLocal(algorithm).
                setGlobal(EmptyClustering.provider()).
                build(graph);
    }

    /**
     * Construct an instance of {@link Watset}.
     *
     * @param algorithm the local clustering algorithm provider
     * @param graph     the graph
     * @return an instance of Watset
     */
    @SuppressWarnings("deprecation")
    public Watset<String, DefaultWeightedEdge> getWatset(AlgorithmProvider<String, DefaultWeightedEdge> algorithm, Graph<String, DefaultWeightedEdge> graph) {
        return new Watset.Builder<String, DefaultWeightedEdge>().
                setLocal(algorithm).
                setGlobal(EmptyClustering.provider()).
                build(graph);
    }
}
