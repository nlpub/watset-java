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
import org.nlpub.watset.util.IndexedSense;
import org.nlpub.watset.util.Sense;

import java.util.*;
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
public class SimplifiedWatset<V, E> implements Clustering<V> {
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
        return graph -> new SimplifiedWatset<>(graph, local, global);
    }

    private static final Logger logger = Logger.getLogger(SimplifiedWatset.class.getSimpleName());

    private final Graph<V, E> graph;
    private final SenseInduction<V, E> inducer;
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global;
    private Graph<Sense<V>, DefaultWeightedEdge> senseGraph;
    private Collection<Collection<Sense<V>>> senseClusters;
    private Map<V, Map<V, Integer>> inventory;
    private Map<V, List<Sense<V>>> senses;

    /**
     * Create an instance of the Simplified Watset clustering algorithm.
     *
     * @param graph  the graph
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     */
    public SimplifiedWatset(Graph<V, E> graph, Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global) {
        this.graph = requireNonNull(graph);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
        this.global = requireNonNull(global);
    }

    @Override
    public void fit() {
        senseClusters = null;

        inventory = new ConcurrentHashMap<>(graph.vertexSet().size());
        senses = new ConcurrentHashMap<>(graph.vertexSet().size());

        logger.info("Simplified Watset started.");

        graph.vertexSet().parallelStream().forEach(node -> {
            final var clusters = inducer.clusters(node);

            if (nonNull(inventory.put(node, new HashMap<>()))) {
                throw new IllegalStateException("The target node is already in the inventory");
            }

            if (nonNull(senses.put(node, new ArrayList<>(clusters.size())))) {
                throw new IllegalStateException("The target node is already in the sense index");
            }

            if (clusters.isEmpty()) {
                senses.get(node).add(new IndexedSense<>(node, 0));
            }

            var i = 0;

            for (final var cluster : clusters) {
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
        globalClustering.fit();

        logger.info("Simplified Watset: extracting sense clusters.");

        senseClusters = globalClustering.getClusters();

        logger.info("Simplified Watset finished.");
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return requireNonNull(senseClusters, "call fit() first").stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toSet());
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
     * Get the disambiguated contexts built during {@link #fit()}.
     *
     * @return the disambiguated contexts
     */
    public Map<Sense<V>, Map<Sense<V>, Number>> getContexts() {
        final var contexts = new HashMap<Sense<V>, Map<Sense<V>, Number>>();

        for (final var edge : requireNonNull(senseGraph, "call fit() first").edgeSet()) {
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
