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
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * A faster and simplified version of Watset that does not need a context similarity measure.
 * <p>
 * This is the recommended implementation of the Watset clustering algorithm.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 */
public class SimplifiedWatset<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link SimplifiedWatset}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringBuilder<V, E, SimplifiedWatset<V, E>> {
        private Function<Graph<V, E>, ClusteringAlgorithm<V>> local;
        private Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global;

        @Override
        public SimplifiedWatset<V, E> build(Graph<V, E> graph) {
            return new SimplifiedWatset<>(graph, local, global);
        }

        @Override
        public Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
            return SimplifiedWatset.provider(local, global);
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
         * @param localBuilder the local clustering algorithm builder
         * @return the builder
         */
        public Builder<V, E> setLocalBuilder(ClusteringBuilder<V, E, ?> localBuilder) {
            this.local = requireNonNull(localBuilder).provider();
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
         * @param globalBuilder the global clustering algorithm builder
         * @return the builder
         */
        public Builder<V, E> setGlobalBuilder(ClusteringBuilder<Sense<V>, DefaultWeightedEdge, ?> globalBuilder) {
            this.global = requireNonNull(globalBuilder).provider();
            return this;
        }
    }

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     * @param <V>    the type of nodes in the graph
     * @param <E>    the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    public static <V, E> Function<Graph<V, E>, ClusteringAlgorithm<V>> provider(Function<Graph<V, E>, ClusteringAlgorithm<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global) {
        return graph -> new SimplifiedWatset<>(graph, local, global);
    }

    private static final Logger logger = Logger.getLogger(SimplifiedWatset.class.getSimpleName());

    /**
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * The node sense induction approach.
     */
    protected final SenseInduction<V, E> inducer;

    /**
     * The global clustering algorithm supplier.
     */
    protected final Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global;

    /**
     * The sense graph.
     */
    protected Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

    /**
     * The node sense clusters.
     */
    protected Clustering<Sense<V>> senseClusters;

    /**
     * The sense inventory.
     */
    protected Map<V, Map<V, Integer>> inventory;

    /**
     * The sense mapping.
     */
    protected Map<V, List<Sense<V>>> senses;

    /**
     * Create an instance of the Simplified Watset clustering algorithm.
     *
     * @param graph  the graph
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     */
    public SimplifiedWatset(Graph<V, E> graph, Function<Graph<V, E>, ClusteringAlgorithm<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, ClusteringAlgorithm<Sense<V>>> global) {
        this.graph = requireNonNull(graph);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
        this.global = requireNonNull(global);
    }

    @Override
    public Clustering<V> getClustering() {
        senseClusters = null;

        inventory = new ConcurrentHashMap<>(graph.vertexSet().size());
        senses = new ConcurrentHashMap<>(graph.vertexSet().size());

        logger.info("Simplified Watset started.");

        graph.vertexSet().parallelStream().forEach(node -> {
            final var clustering = inducer.clustering(node);

            if (nonNull(inventory.put(node, new HashMap<>()))) {
                throw new IllegalStateException("The target node is already in the inventory");
            }

            if (nonNull(senses.put(node, new ArrayList<>(clustering.getNumberClusters())))) {
                throw new IllegalStateException("The target node is already in the sense index");
            }

            if (clustering.getNumberClusters() == 0) {
                senses.get(node).add(new IndexedSense<>(node, 0));
            }

            var i = 0;

            for (final var cluster : clustering) {
                senses.get(node).add(i, new IndexedSense<>(node, i));

                for (final var neighbor : cluster) {
                    inventory.get(node).put(neighbor, i);
                }

                i++;
            }
        });

        final var count = senses.values().stream().mapToInt(List::size).sum();
        logger.log(Level.INFO, "Simplified Watset: sense inventory constructed including {0} senses.", count);

        final var builder = SimpleWeightedGraph.<Sense<V>, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class);

        for (final var sourceEntry : inventory.entrySet()) {
            if (sourceEntry.getValue().isEmpty()) {
                builder.addVertex(new IndexedSense<>(sourceEntry.getKey(), 0));
            }

            final var source = sourceEntry.getKey();

            for (final var target : sourceEntry.getValue().keySet()) {
                final var sourceSense = requireNonNull(senses.get(source).get(inventory.get(source).get(target)));
                final var targetSense = requireNonNull(senses.get(target).get(inventory.get(target).get(source)));

                final var edge = requireNonNull(graph.getEdge(source, target));
                final var weight = graph.getEdgeWeight(edge);

                builder.addEdge(sourceSense, targetSense, weight);
            }
        }

        senseGraph = builder.build();

        if (graph.edgeSet().size() != senseGraph.edgeSet().size()) {
            throw new IllegalStateException("Mismatch in number of edges: expected " +
                    graph.edgeSet().size() +
                    ", but got " +
                    senseGraph.edgeSet().size());
        }

        logger.info("Simplified Watset: sense graph constructed.");

        final var globalClustering = global.apply(senseGraph);
        senseClusters = globalClustering.getClustering();

        logger.info("Simplified Watset finished.");

        final var clusters = senseClusters.getClusters().stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toList());

        return new ClusteringImpl<>(clusters);
    }

    /**
     * Get the intermediate node sense graph built during {@link #getClustering()}.
     *
     * @return the sense graph
     */
    public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
        return new AsUnmodifiableGraph<>(requireNonNull(senseGraph, "call getClustering() first"));
    }

    /**
     * Get the disambiguated contexts built during {@link #getClustering()}.
     *
     * @return the disambiguated contexts
     */
    public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
        final var contexts = new HashMap<Sense<V>, Map<Sense<V>, Number>>();

        for (final var edge : requireNonNull(senseGraph, "call getClustering() first").edgeSet()) {
            final Sense<V> source = senseGraph.getEdgeSource(edge), target = senseGraph.getEdgeTarget(edge);
            final var weight = senseGraph.getEdgeWeight(edge);

            if (!contexts.containsKey(source)) contexts.put(source, new HashMap<>());
            if (!contexts.containsKey(target)) contexts.put(target, new HashMap<>());

            contexts.get(source).put(target, weight);
            contexts.get(target).put(source, weight);
        }

        if (contexts.size() != senseGraph.vertexSet().size()) {
            throw new IllegalStateException("Mismatch in number of senses: expected " +
                    senseGraph.vertexSet().size() +
                    ", but got " +
                    contexts.size());
        }

        return contexts;
    }
}
