package org.nlpub.watset.cli;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.Sense;

import java.util.function.Function;

/**
 * Routines for constructing instances of two variations of Watset.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public interface WatsetGetter<V, E> {
    /**
     * Construct an instance of {@link SimplifiedWatset}.
     *
     * @param local the local clustering algorithm provider
     * @param graph the graph
     * @return an instance of Simplified Watset
     */
    default SimplifiedWatset<V, E> getSimplifiedWatset(Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global, Graph<V, E> graph) {
        return new SimplifiedWatset.Builder<V, E>().
                setLocal(local).
                setGlobal(global).
                build(graph);
    }

    /**
     * Construct an instance of {@link Watset}.
     *
     * @param local the local clustering algorithm provider
     * @param graph the graph
     * @return an instance of Watset
     */
    @SuppressWarnings("deprecation")
    default Watset<V, E> getWatset(Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global, Graph<V, E> graph) {
        return new Watset.Builder<V, E>().
                setLocal(local).
                setGlobal(global).
                build(graph);
    }
}
