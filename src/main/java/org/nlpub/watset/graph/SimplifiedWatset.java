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
import org.nlpub.watset.wsi.IndexedSense;
import org.nlpub.watset.wsi.Sense;
import org.nlpub.watset.wsi.SenseInduction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * A simplified version of Watset that does not need a context similarity measure.
 *
 * @param <V> node class.
 * @param <E> edge class.
 * @see Watset
 */
public class SimplifiedWatset<V, E> implements Clustering<V> {
    private static final Logger logger = Logger.getLogger(SimplifiedWatset.class.getSimpleName());

    private final Graph<V, E> graph;
    private final SenseInduction<V, E> inducer;
    private final Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global;
    private Graph<Sense<V>, DefaultWeightedEdge> senseGraph;
    private Collection<Collection<Sense<V>>> senseClusters;
    private Map<V, Map<V, Integer>> inventory;
    private Map<V, List<Sense<V>>> senses;

    /**
     * Sets up the Watset clustering algorithm.
     *
     * @param graph  an input graph.
     * @param local  a supplier for a local clustering algorithm.
     * @param global a supplier for a global clustering algorithm.
     */
    public SimplifiedWatset(Graph<V, E> graph, Function<Graph<V, E>, Clustering<V>> local, Function<Graph<Sense<V>, DefaultWeightedEdge>, Clustering<Sense<V>>> global) {
        this.graph = requireNonNull(graph);
        this.inducer = new SenseInduction<>(graph, requireNonNull(local));
        this.global = requireNonNull(global);
    }

    @Override
    public Clustering<V> fit() {
        inventory = new ConcurrentHashMap<>(graph.vertexSet().size());
        senses = new ConcurrentHashMap<>(graph.vertexSet().size());

        logger.info("Watset started.");

        graph.vertexSet().parallelStream().forEach(node -> {
            final Collection<Collection<V>> clusters = inducer.clusters(node);

            if (!isNull(inventory.put(node, new HashMap<>()))) {
                throw new IllegalStateException("The target node is already in the inventory");
            }

            if (!isNull(senses.put(node, new ArrayList<>(clusters.size())))) {
                throw new IllegalStateException("The target node is already in the sense index");
            }

            if (clusters.isEmpty()) {
                senses.get(node).add(new IndexedSense<>(node, 0));
            }

            int i = 0;

            for (final Collection<V> cluster : clusters) {
                senses.get(node).add(i, new IndexedSense<>(node, i));

                for (final V neighbor : cluster) {
                    inventory.get(node).put(neighbor, i);
                }

                i++;
            }
        });

        final int count = senses.values().stream().mapToInt(List::size).sum();
        logger.log(Level.INFO, "Watset: sense inventory constructed having {0} senses.", count);

        final GraphBuilder<Sense<V>, DefaultWeightedEdge, ? extends SimpleWeightedGraph<Sense<V>, DefaultWeightedEdge>> builder = SimpleWeightedGraph.createBuilder(DefaultWeightedEdge.class);

        for (final Map.Entry<V, Map<V, Integer>> sourceEntry : inventory.entrySet()) {
            if (sourceEntry.getValue().isEmpty()) {
                builder.addVertex(new IndexedSense<>(sourceEntry.getKey(), 0));
            }

            final V source = sourceEntry.getKey();

            for (final V target : sourceEntry.getValue().keySet()) {
                final Sense<V> sourceSense = requireNonNull(senses.get(source).get(inventory.get(source).get(target)));
                final Sense<V> targetSense = requireNonNull(senses.get(target).get(inventory.get(target).get(source)));

                final E edge = requireNonNull(graph.getEdge(source, target));
                final double weight = graph.getEdgeWeight(edge);

                builder.addEdge(sourceSense, targetSense, weight);
            }
        }

        senseGraph = builder.build();

        if (graph.edgeSet().size() != senseGraph.edgeSet().size()) {
            throw new IllegalStateException("Mismatch in the number of edges: expected " +
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

        return this;
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return senseClusters.stream().
                map(cluster -> cluster.stream().map(Sense::get).collect(Collectors.toSet())).
                collect(Collectors.toSet());
    }

    /**
     * Gets an intermediate sense-aware graph.
     *
     * @return a sense graph.
     */
    public Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph() {
        return new AsUnmodifiableGraph<>(requireNonNull(senseGraph));
    }
}
