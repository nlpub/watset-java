/*
 * Copyright 2019 Dmitry Ustalov
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

package org.nlpub.watset.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.nlpub.watset.util.ContextSimilarity;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Watset is a local-global meta-algorithm for fuzzy graph clustering.
 * <p>
 * Watset builds an intermediate undirected graph by inducing different senses of each node in the input graph.
 * <p>
 * We recommend using {@link SimplifiedWatset} instead of this class.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 * @see SimplifiedWatset
 */
public class Watset<V, E> implements Clustering<V> {
    /**
     * Watset inserts the target node during disambiguation.
     * This constant specifies its weight which is equal to one.
     */
    private final static Number DEFAULT_CONTEXT_WEIGHT = 1;

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     * @param <V>    the type of nodes in the graph
     * @param <E>    the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global) {
        return graph -> new Watset<>(graph, local, global, new CosineContextSimilarity<>());
    }

    private static final Logger logger = Logger.getLogger(Watset.class.getSimpleName());

    private final Graph<V, E> graph;
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global;
    private final ContextSimilarity<V> similarity;
    private final SenseInduction<V, E> inducer;
    private Map<V, Map<Sense<V>, Map<V, Number>>> inventory;
    private Map<Sense<V>, Map<Sense<V>, Number>> contexts;
    private Collection<Collection<Sense<V>>> senseClusters;
    private Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

    /**
     * Create an instance of the Watset clustering algorithm.
     *
     * @param graph      the graph
     * @param local      the local clustering algorithm supplier
     * @param global     the global clustering algorithm supplier
     * @param similarity the context similarity measure
     */
    public Watset(Graph<V, E> graph, Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global, ContextSimilarity<V> similarity) {
        this.graph = requireNonNull(graph);
        this.global = requireNonNull(global);
        this.similarity = requireNonNull(similarity);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
    }

    @Override
    public void fit() {
        senseClusters = null;
        senseGraph = null;
        inventory = null;
        contexts = null;

        logger.info("Watset started.");

        inventory = new ConcurrentHashMap<>();

        graph.vertexSet().parallelStream().forEach(node -> {
            final List<Map<V, Number>> senses = inducer.contexts(node);

            final Map<Sense<V>, Map<V, Number>> senseMap = new HashMap<>(senses.size());

            for (int i = 0; i < senses.size(); i++) {
                senseMap.put(new IndexedSense<>(node, i), senses.get(i));
            }

            inventory.put(node, senseMap);
        });

        final int senses = inventory.values().stream().mapToInt(Map::size).sum();

        logger.log(Level.INFO, "Watset: sense inventory constructed including {0} senses.", senses);

        contexts = new ConcurrentHashMap<>(senses);

        inventory.entrySet().parallelStream().forEach(wordSenses -> {
            if (wordSenses.getValue().isEmpty()) {
                contexts.put(new IndexedSense<>(wordSenses.getKey(), 0), Collections.emptyMap());
            } else {
                wordSenses.getValue().forEach((sense, context) ->
                        contexts.put(sense, disambiguateContext(inventory, sense)));
            }
        });

        logger.info("Watset: contexts constructed.");

        senseGraph = buildSenseGraph(contexts);

        if (graph.edgeSet().size() > senseGraph.edgeSet().size()) {
            throw new IllegalStateException("Mismatch in number of edges: expected at least " +
                    graph.edgeSet().size() +
                    ", but got " +
                    senseGraph.edgeSet().size());
        }

        logger.info("Watset: sense graph constructed.");

        final Clustering<Sense<V>> globalClustering = global.apply(senseGraph);
        globalClustering.fit();

        logger.info("Watset: extracting sense clusters.");

        senseClusters = globalClustering.getClusters();

        logger.info("Watset finished.");
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return requireNonNull(senseClusters, "call fit() first").stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toSet());
    }

    /**
     * Get the sense inventory built during {@link #fit()}.
     *
     * @return the sense inventory
     */
    @SuppressWarnings("unused")
    public Map<V, Map<Sense<V>, Map<V, Number>>> getInventory() {
        return Collections.unmodifiableMap(requireNonNull(inventory, "call fit() first"));
    }

    /**
     * Get the disambiguated contexts built during {@link #fit()}.
     *
     * @return the disambiguated contexts
     */
    public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
        return Collections.unmodifiableMap(requireNonNull(contexts, "call fit() first"));
    }

    /**
     * Get the intermediate node sense graph built during {@link #fit()}.
     *
     * @return the sense graph
     */
    public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
        return new AsUnmodifiableGraph<>(requireNonNull(senseGraph, "call fit() first"));
    }

    /**
     * Disambiguate the context of the given node sense as according to the sense inventory
     * using {@link Sense#disambiguate(Map, ContextSimilarity, Map, Collection)}.
     *
     * @param inventory the sense inventory
     * @param sense     the target sense
     * @return the disambiguated context of {@code sense}
     */
    private Map<Sense<V>, Number> disambiguateContext(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, Sense<V> sense) {
        final Map<V, Number> context = new HashMap<>(inventory.get(sense.get()).get(sense));

        context.put(sense.get(), DEFAULT_CONTEXT_WEIGHT);

        return Sense.disambiguate(inventory, similarity, context, Collections.singleton(sense.get()));
    }

    /**
     * Build an intermediate sense-aware representation of the input graph called the <em>node sense graph</em>.
     *
     * @param contexts the disambiguated contexts
     * @return the sense graph
     */
    private Graph<Sense<V>, DefaultWeightedEdge> buildSenseGraph(Map<Sense<V>, Map<Sense<V>, Number>> contexts) {
        final GraphBuilder<Sense<V>, DefaultWeightedEdge, ? extends SimpleWeightedGraph<Sense<V>, DefaultWeightedEdge>> builder = SimpleWeightedGraph.createBuilder(DefaultWeightedEdge.class);

        contexts.keySet().forEach(builder::addVertex);

        contexts.forEach((source, context) ->
                context.forEach((target, weight) ->
                        builder.addEdge(source, target, weight.doubleValue())));

        return builder.build();
    }
}
