/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.watset;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.nlpub.graph.Clustering;
import org.nlpub.vsm.ContextSimilarity;
import org.nlpub.watset.sense.Sense;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.nlpub.vsm.ContextSimilarity.DEFAULT_CONTEXT_WEIGHT;
import static org.nlpub.watset.sense.Sense.disambiguate;

public class Watset<V, E> implements Clustering<V> {
    private static final Logger logger = Logger.getLogger(Watset.class.getSimpleName());

    private final Graph<V, E> graph;
    private final Function<Graph<V, E>, Clustering<V>> localClusteringProvider;
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> globalClusteringProvider;
    private final ContextSimilarity<V> similarity;
    private Map<V, Map<Sense<V>, Map<V, Number>>> inventory;
    private Collection<Collection<Sense<V>>> senseClusters;
    private Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

    public Watset(Graph<V, E> graph, Function<Graph<V, E>, Clustering<V>> localClusteringProvider, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> globalClusteringProvider, ContextSimilarity<V> similarity) {
        this.graph = graph;
        this.localClusteringProvider = localClusteringProvider;
        this.globalClusteringProvider = globalClusteringProvider;
        this.similarity = similarity;
    }

    @Override
    public void run() {
        this.senseClusters = null;
        this.senseGraph = null;
        this.inventory = null;

        logger.info("Watset started.");

        inventory = graph.vertexSet().parallelStream().
                collect(Collectors.toMap(Function.identity(), this::induceSenses));

        logger.info("Watset: sense inventory constructed.");

        final Map<Sense<V>, Map<Sense<V>, Number>> contexts = new ConcurrentHashMap<>();

        inventory.entrySet().parallelStream().forEach(wordSenses ->
                wordSenses.getValue().forEach((sense, context) ->
                        contexts.put(sense, disambiguateContext(inventory, sense)))
        );

        logger.info("Watset: contexts constructed.");

        this.senseGraph = buildSenseGraph(contexts);

        logger.info("Watset: sense graph constructed.");

        final Clustering<Sense<V>> globalClustering = this.globalClusteringProvider.apply(senseGraph);
        globalClustering.run();

        logger.info("Watset: clustering the sense graph.");

        this.senseClusters = globalClustering.getClusters();

        logger.info("Watset finished.");
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return this.senseClusters.stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toSet());
    }

    public Map<V, Map<Sense<V>, Map<V, Number>>> getInventory() {
        if (Objects.isNull(inventory)) {
            throw new IllegalStateException("The sense inventory is not yet initialized.");
        }

        return inventory;
    }

    public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
        if (Objects.isNull(senseGraph)) {
            throw new IllegalStateException("The sense graph is not yet initialized.");
        }

        return senseGraph;
    }

    protected Map<Sense<V>, Map<V, Number>> induceSenses(V target) {
        final SenseInduction<V, E> inducer = new SenseInduction<>(graph, target, this.localClusteringProvider);
        inducer.run();

        return inducer.getSenses();
    }

    protected Map<Sense<V>, Number> disambiguateContext(Map<V, Map<Sense<V>, Map<V, Number>>> inventory, Sense<V> sense) {
        final Map<V, Number> context = new HashMap<>(inventory.get(sense.get()).get(sense));
        context.put(sense.get(), DEFAULT_CONTEXT_WEIGHT);

        return disambiguate(inventory, similarity, context, Collections.singleton(sense.get()));
    }

    protected Graph<Sense<V>, DefaultWeightedEdge> buildSenseGraph(Map<Sense<V>, Map<Sense<V>, Number>> contexts) {
        final GraphBuilder<Sense<V>, DefaultWeightedEdge, SimpleWeightedGraph<Sense<V>, DefaultWeightedEdge>> builder = new GraphBuilder<>(new SimpleWeightedGraph<>(DefaultWeightedEdge.class));

        contexts.keySet().forEach(builder::addVertex);

        contexts.forEach((source, context) ->
                context.forEach((target, weight) ->
                        builder.addEdge(source, target, weight.doubleValue())));

        return builder.build();
    }
}
