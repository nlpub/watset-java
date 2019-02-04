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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.nlpub.watset.util.ContextSimilarity;
import org.nlpub.watset.util.CosineContextSimilarity;
import org.nlpub.watset.wsi.Sense;
import org.nlpub.watset.wsi.SenseInduction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Watset is a local-global meta-algorithm for fuzzy graph clustering.
 * It builds an intermediate undirected graph that addresses the element ambiguity by
 * inducing different senses of each node in the input graph.
 *
 * @param <V> node class.
 * @param <E> edge class.
 * @see <a href="https://doi.org/10.18653/v1/P17-1145">Ustalov et al. (ACL 2017)</a>
 */
public class Watset<V, E> implements Clustering<V> {
    /**
     * Watset inserts the target node during disambiguation.
     * This constant specifies its weight which is equal to one.
     */
    private final static Number DEFAULT_CONTEXT_WEIGHT = 1;

    /**
     * Sets up the Watset clustering algorithm in a functional style.
     *
     * @param local  a supplier for a local clustering algorithm.
     * @param global a supplier for a global clustering algorithm.
     * @param <V>    node class.
     * @param <E>    edge class.
     * @return an instance of Watset.
     */
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global) {
        return graph -> new Watset<>(graph, local, global, new CosineContextSimilarity<>());
    }

    private static final Logger logger = Logger.getLogger(Watset.class.getSimpleName());

    private final Graph<V, E> graph;
    private final Function<Graph<V, E>, Clustering<V>> local;
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global;
    private final ContextSimilarity<V> similarity;
    private Map<V, Map<Sense<V>, Map<V, Number>>> inventory;
    private Collection<Collection<Sense<V>>> senseClusters;
    private Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

    /**
     * Sets up the Watset clustering algorithm.
     *
     * @param graph      an input graph.
     * @param local      a supplier for a local clustering algorithm.
     * @param global     a supplier for a global clustering algorithm.
     * @param similarity a context similarity measure.
     */
    public Watset(Graph<V, E> graph, Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global, ContextSimilarity<V> similarity) {
        this.graph = requireNonNull(graph);
        this.local = requireNonNull(local);
        this.global = requireNonNull(global);
        this.similarity = requireNonNull(similarity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        senseClusters = null;
        senseGraph = null;
        inventory = null;

        logger.info("Watset started.");

        inventory = graph.vertexSet().parallelStream().
                collect(Collectors.toMap(Function.identity(), this::induceSenses));

        final int senses = inventory.values().stream().mapToInt(Map::size).sum();

        logger.log(Level.INFO, "Watset: sense inventory constructed having {0} senses.", senses);

        final Map<Sense<V>, Map<Sense<V>, Number>> contexts = new ConcurrentHashMap<>(senses);

        inventory.entrySet().parallelStream().forEach(wordSenses ->
                wordSenses.getValue().forEach((sense, context) ->
                        contexts.put(sense, disambiguateContext(inventory, sense)))
        );

        logger.info("Watset: contexts constructed.");

        senseGraph = buildSenseGraph(contexts);

        logger.info("Watset: sense graph constructed.");

        final Clustering<Sense<V>> globalClustering = global.apply(senseGraph);
        globalClustering.run();

        logger.info("Watset: extracting sense clusters.");

        senseClusters = globalClustering.getClusters();

        logger.info("Watset finished.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Collection<V>> getClusters() {
        return senseClusters.stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toSet());
    }

    /**
     * Gets a built sense inventory.
     *
     * @return a sense inventory.
     */
    public Map<V, Map<Sense<V>, Map<V, Number>>> getInventory() {
        if (Objects.isNull(inventory)) {
            throw new IllegalStateException("The sense inventory is not yet initialized.");
        }

        return inventory;
    }

    /**
     * Gets an intermediate sense-aware graph.
     *
     * @return a sense graph.
     */
    public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
        if (Objects.isNull(senseGraph)) {
            throw new IllegalStateException("The sense graph is not yet initialized.");
        }

        return senseGraph;
    }

    /**
     * Induces senses for the target node.
     *
     * @param target a target node.
     * @return a map to senses to contexts.
     */
    private Map<Sense<V>, Map<V, Number>> induceSenses(V target) {
        final SenseInduction<V, E> inducer = new SenseInduction<>(graph, target, local);

        inducer.run();

        return inducer.getSenses();
    }

    /**
     * Disambiguates the context of the given node sense as according to the sense inventory
     * using {@link Sense#disambiguate(Map, ContextSimilarity, Map, Set)}.
     *
     * @param inventory a sense inventory.
     * @param sense     a target sense.
     * @return a disambiguated context.
     */
    private Map<Sense<V>, Number> disambiguateContext(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, Sense<V> sense) {
        final Map<V, Number> context = new HashMap<>(inventory.get(sense.get()).get(sense));

        context.put(sense.get(), DEFAULT_CONTEXT_WEIGHT);

        return Sense.disambiguate(inventory, similarity, context, Collections.singleton(sense.get()));
    }

    /**
     * Builds an intermediate sense-aware representation of the input graph.
     *
     * @param contexts disambiguated contexts.
     * @return a sense graph.
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
