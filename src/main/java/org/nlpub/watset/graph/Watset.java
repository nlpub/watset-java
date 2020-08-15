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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.*;
import static org.jgrapht.GraphTests.requireUndirected;

/**
 * Watset is a local-global meta-algorithm for fuzzy graph clustering.
 * <p>
 * Watset builds an intermediate undirected graph by inducing different senses of each node in the input graph.
 * <p>
 * This implementation of Watset is known as Simplified Watset. It does not need a context similarity measure.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 */
public class Watset<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link Watset}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, Watset<V, E>> {
        private ClusteringAlgorithmBuilder<V, E, ?> local;
        private ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global;

        @Override
        public Watset<V, E> apply(Graph<V, E> graph) {
            return new Watset<>(graph, local, global);
        }

        /**
         * Set the local clustering algorithm supplier.
         *
         * @param local the local clustering algorithm supplier
         * @return the builder
         */
        public Builder<V, E> setLocal(ClusteringAlgorithmBuilder<V, E, ?> local) {
            this.local = requireNonNull(local);
            return this;
        }

        /**
         * Set the global clustering algorithm supplier.
         *
         * @param global the global clustering algorithm supplier
         * @return the builder
         */
        public Builder<V, E> setGlobal(ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global) {
            this.global = requireNonNull(global);
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

    private static final Logger logger = Logger.getLogger(Watset.class.getSimpleName());

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
    protected final ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global;

    /**
     * The cached clustering result.
     */
    protected WatsetClustering<V> clustering;

    /**
     * Create an instance of the Watset clustering algorithm.
     *
     * @param graph  the graph
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     */
    public Watset(Graph<V, E> graph, ClusteringAlgorithmBuilder<V, E, ?> local, ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global) {
        this.graph = requireUndirected(graph);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
        this.global = requireNonNull(global);
    }

    @Override
    public WatsetClustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph, inducer, global).compute();
        }

        return clustering;
    }

    /**
     * Actual implementation of Simplified Watset.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class Implementation<V, E> {
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
        protected final ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global;

        /**
         * The sense inventory.
         */
        protected final Map<V, Map<V, Integer>> inventory;

        /**
         * The sense mapping.
         */
        protected final Map<V, List<Sense<V>>> senses;

        /**
         * Create an instance of the Watset clustering algorithm implementation.
         *
         * @param graph   the graph
         * @param inducer the node sense induction approach
         * @param global  the global clustering algorithm supplier
         */
        public Implementation(Graph<V, E> graph, SenseInduction<V, E> inducer, ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global) {
            this.graph = graph;
            this.inducer = inducer;
            this.global = global;
            this.inventory = new ConcurrentHashMap<>(graph.vertexSet().size());
            this.senses = new ConcurrentHashMap<>(graph.vertexSet().size());
        }

        /**
         * Perform clustering with Watset.
         *
         * @return the clustering
         */
        public WatsetClustering<V> compute() {
            logger.info("Watset started.");

            buildInventory();

            final var count = senses.values().stream().mapToInt(List::size).sum();
            logger.log(Level.INFO, "Watset: sense inventory constructed including {0} senses.", count);

            final var senseGraph = buildSenseGraph();

            if (graph.edgeSet().size() != senseGraph.edgeSet().size()) {
                throw new IllegalStateException("Mismatch in number of edges: expected " +
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
                    new AsUnmodifiableGraph<>(senseGraph));
        }

        /**
         * Build a node sense inventory; fill in variables {@code senses} and {@code inventory}.
         */
        protected void buildInventory() {
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
        }

        /**
         * Build an intermediate sense-aware representation of the input graph called the <em>node sense graph</em>.
         *
         * @return the sense graph
         */
        protected Graph<Sense<V>, DefaultWeightedEdge> buildSenseGraph() {
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

            return builder.build();
        }
    }

    /**
     * A Watset clustering that computes disambiguated contexts on demand.
     *
     * @param <V> the type of nodes in the graph
     */
    static class WatsetClusteringImpl<V> extends ClusteringAlgorithm.ClusteringImpl<V> implements WatsetClustering<V> {
        /**
         * The sense inventory.
         */
        private final Map<V, Map<V, Integer>> inventory;

        /**
         * The sense graph.
         */
        private final Graph<Sense<V>, DefaultWeightedEdge> senseGraph;

        /**
         * The cached disambiguated contexts.
         */
        private Map<Sense<V>, Map<Sense<V>, Number>> contexts;

        /**
         * Construct a new Watset clustering.
         *
         * @param clusters   the clusters
         * @param inventory  the sense inventory
         * @param senseGraph the sense graph
         */
        public WatsetClusteringImpl(List<Set<V>> clusters, Map<V, Map<V, Integer>> inventory, Graph<Sense<V>, DefaultWeightedEdge> senseGraph) {
            super(clusters);
            this.inventory = inventory;
            this.senseGraph = senseGraph;
        }

        /**
         * Get the sense inventory built during {@link Watset#getClustering()}.
         *
         * @return the sense inventory
         */
        @SuppressWarnings("unused")
        public Map<V, Map<V, Integer>> getInventory() {
            return inventory;
        }

        @Override
        public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
            return senseGraph;
        }

        @Override
        public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
            if (isNull(contexts)) {
                contexts = new HashMap<>();

                for (final var edge : senseGraph.edgeSet()) {
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
            }

            return Collections.unmodifiableMap(contexts);
        }
    }
}
