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
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.nlpub.watset.util.ContextSimilarities;
import org.nlpub.watset.util.ContextSimilarity;
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.jgrapht.GraphTests.requireUndirected;

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
 * @deprecated Replaced with {@link SimplifiedWatset}
 */
@Deprecated
public class Watset<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link Watset}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, Watset<V, E>> {
        private Function<Graph<V, E>, ClusteringAlgorithm<V>> local;
        private Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global;
        private ContextSimilarity<V> similarity = ContextSimilarities.cosine();

        @Override
        public Watset<V, E> build(Graph<V, E> graph) {
            return new Watset<>(graph, local, global, similarity);
        }

        @Override
        public Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
            return Watset.provider(local, global, similarity);
        }

        /**
         * Set the local clustering algorithm supplier.
         *
         * @param local the local clustering algorithm supplier
         * @return the builder
         */
        public Builder<V, E> setLocal(Function<Graph<V, E>, ClusteringAlgorithm<V>> local) {
            this.local = requireNonNull(local);
            return this;
        }

        /**
         * Set the local clustering algorithm builder.
         *
         * @param local the local clustering algorithm builder
         * @return the builder
         */
        public Builder<V, E> setLocal(ClusteringAlgorithmBuilder<V, E, ?> local) {
            this.local = requireNonNull(local).provider();
            return this;
        }

        /**
         * Set the global clustering algorithm supplier.
         *
         * @param global the global clustering algorithm supplier
         * @return the builder
         */
        public Builder<V, E> setGlobal(Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global) {
            this.global = requireNonNull(global);
            return this;
        }

        /**
         * Set the global clustering algorithm builder.
         *
         * @param global the global clustering algorithm builder
         * @return the builder
         */
        public Builder<V, E> setGlobal(ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global) {
            this.global = requireNonNull(global).provider();
            return this;
        }

        /**
         * Set the context similarity measure.
         *
         * @param similarity the context similarity measure
         * @return the builder
         */
        public Builder<V, E> setSimilarity(ContextSimilarity<V> similarity) {
            this.similarity = requireNonNull(similarity);
            return this;
        }
    }

    /**
     * Create a builder.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a builder
     */
    public static <V, E> Builder<V, E> builder() {
        return new Builder<>();
    }

    /**
     * Watset inserts the target node during disambiguation.
     * This constant specifies its weight which is equal to one.
     */
    private final static Number DEFAULT_CONTEXT_WEIGHT = 1;

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param local      the local clustering algorithm supplier
     * @param global     the global clustering algorithm supplier
     * @param similarity the context similarity measure
     * @param <V>        the type of nodes in the graph
     * @param <E>        the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    public static <V, E> Function<Graph<V, E>, ClusteringAlgorithm<V>> provider(Function<Graph<V, E>, ClusteringAlgorithm<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global, ContextSimilarity<V> similarity) {
        return graph -> new Watset<>(graph, local, global, similarity);
    }

    private static final Logger logger = Logger.getLogger(Watset.class.getSimpleName());

    /**
     * The graph.
     */
    private final Graph<V, E> graph;

    /**
     * The global clustering algorithm supplier.
     */
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global;

    /**
     * The context similarity measure.
     */
    private final ContextSimilarity<V> similarity;

    /**
     * The node sense induction approach.
     */
    private final SenseInduction<V, E> inducer;

    /**
     * The cached clustering result.
     */
    private WatsetClustering<V> clustering;

    /**
     * Create an instance of the Watset clustering algorithm.
     *
     * @param graph      the graph
     * @param local      the local clustering algorithm supplier
     * @param global     the global clustering algorithm supplier
     * @param similarity the context similarity measure
     */
    public Watset(Graph<V, E> graph, Function<Graph<V, E>, ClusteringAlgorithm<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global, ContextSimilarity<V> similarity) {
        this.graph = requireUndirected(graph);
        this.global = requireNonNull(global);
        this.similarity = requireNonNull(similarity);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
    }

    @Override
    public WatsetClustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph, inducer, global, similarity).compute();
        }

        return clustering;
    }

    /**
     * Actual implementation of Watset.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    private static class Implementation<V, E> {
        /**
         * The graph.
         */
        private final Graph<V, E> graph;

        /**
         * The node sense induction approach.
         */
        private final SenseInduction<V, E> inducer;

        /**
         * The global clustering algorithm supplier.
         */
        private final Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global;

        /**
         * The context similarity measure.
         */
        private final ContextSimilarity<V> similarity;

        /**
         * The sense inventory.
         */
        private final Map<V, Map<Sense<V>, Map<V, Number>>> inventory;

        /**
         * Create an instance of the Watset clustering algorithm implementation.
         *
         * @param graph      the graph
         * @param inducer    the node sense induction approach
         * @param global     the global clustering algorithm supplier
         * @param similarity the context similarity measure
         */
        private Implementation(Graph<V, E> graph, SenseInduction<V, E> inducer, Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global, ContextSimilarity<V> similarity) {
            this.graph = graph;
            this.inducer = inducer;
            this.global = global;
            this.similarity = similarity;
            this.inventory = new ConcurrentHashMap<>(graph.vertexSet().size());
        }

        /**
         * Perform clustering with Watset.
         *
         * @return the clustering
         */
        public WatsetClustering<V> compute() {
            logger.info("Watset started.");

            buildInventory();

            final var senses = inventory.values().stream().mapToInt(Map::size).sum();

            logger.log(Level.INFO, "Watset: sense inventory constructed including {0} senses.", senses);

            final var contexts = buildContexts(senses);

            logger.info("Watset: contexts constructed.");

            final var senseGraph = buildSenseGraph(contexts);

            if (graph.edgeSet().size() > senseGraph.edgeSet().size()) {
                throw new IllegalStateException("Mismatch in number of edges: expected at least " +
                        graph.edgeSet().size() +
                        ", but got " +
                        senseGraph.edgeSet().size());
            }

            logger.info("Watset: sense graph constructed.");

            final var globalAlgorithm = global.apply(senseGraph);
            final var senseClusters = globalAlgorithm.getClustering();

            logger.info("Watset finished.");

            final var clusters = senseClusters.getClusters().stream().
                    map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                    collect(Collectors.toList());

            return new WatsetClusteringImpl<>(clusters,
                    Collections.unmodifiableMap(inventory),
                    new AsUnmodifiableGraph<>(senseGraph),
                    Collections.unmodifiableMap(contexts));
        }

        /**
         * Build a node sense inventory; fill in variable {@code inventory}.
         */
        private void buildInventory() {
            graph.vertexSet().parallelStream().forEach(node -> {
                final var senses = inducer.contexts(node);

                final Map<Sense<V>, Map<V, Number>> senseMap = new HashMap<>(senses.size());

                for (var i = 0; i < senses.size(); i++) {
                    senseMap.put(new IndexedSense<>(node, i), senses.get(i));
                }

                inventory.put(node, senseMap);
            });
        }

        /**
         * Build disambiguated contexts.
         *
         * @param senses the total number of senses
         * @return the disambiguated contexts
         */
        private Map<Sense<V>, Map<Sense<V>, Number>> buildContexts(int senses) {
            final var contexts = new ConcurrentHashMap<Sense<V>, Map<Sense<V>, Number>>(senses);

            inventory.entrySet().parallelStream().forEach(wordSenses -> {
                if (wordSenses.getValue().isEmpty()) {
                    contexts.put(new IndexedSense<>(wordSenses.getKey(), 0), Collections.emptyMap());
                } else {
                    wordSenses.getValue().forEach((sense, context) -> contexts.put(sense, disambiguateContext(inventory, sense)));
                }
            });

            return contexts;
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
            final var context = new HashMap<>(inventory.get(sense.get()).get(sense));

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
            final var builder = SimpleWeightedGraph.<Sense<V>, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class);

            contexts.keySet().forEach(builder::addVertex);

            contexts.forEach((source, context) -> context.forEach((target, weight) -> builder.addEdge(source, target, weight.doubleValue())));

            return builder.build();
        }
    }

    /**
     * A Watset clustering that holds pre-computed disambiguated contexts.
     *
     * @param <V> the type of nodes in the graph
     */
    static class WatsetClusteringImpl<V> extends ClusteringAlgorithm.ClusteringImpl<V> implements WatsetClustering<V> {
        /**
         * The sense inventory.
         */
        private final Map<V, Map<Sense<V>, Map<V, Number>>> inventory;

        /**
         * The sense graph.
         */
        private final Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

        /**
         * The disambiguated contexts.
         */
        private final Map<Sense<V>, Map<Sense<V>, Number>> contexts;

        /**
         * Construct a new Watset clustering.
         *
         * @param clusters   the clusters
         * @param inventory  the sense inventory
         * @param senseGraph the sense graph
         * @param contexts   the disambiguated contexts
         */
        public WatsetClusteringImpl(List<Set<V>> clusters, Map<V, Map<Sense<V>, Map<V, Number>>> inventory, Graph<Sense<V>, DefaultWeightedEdge> senseGraph, Map<Sense<V>, Map<Sense<V>, Number>> contexts) {
            super(clusters);
            this.inventory = inventory;
            this.senseGraph = senseGraph;
            this.contexts = contexts;
        }

        /**
         * Get the sense inventory built during {@link Watset#getClustering()}.
         *
         * @return the sense inventory
         */
        @SuppressWarnings("unused")
        public Map<V, Map<Sense<V>, Map<V, Number>>> getInventory() {
            return inventory;
        }

        @Override
        public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
            return senseGraph;
        }

        @Override
        public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
            return contexts;
        }
    }
}
