/*
 * Copyright 2018 Dmitry Ustalov
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

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jgrapht.GraphTests.requireUndirected;

/**
 * A trivial clustering algorithm that puts every node in a separate cluster.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class SingletonClustering<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link SingletonClustering}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, SingletonClustering<V, E>> {
        @Override
        public SingletonClustering<V, E> build(Graph<V, E> graph) {
            return new SingletonClustering<>(graph);
        }

        @Override
        public Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
            return SingletonClustering.provider();
        }
    }

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    public static <V, E> Function<Graph<V, E>, ClusteringAlgorithm<V>> provider() {
        return SingletonClustering::new;
    }

    /**
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * Set up the trivial clustering algorithm that puts every node in a separate cluster.
     *
     * @param graph the graph
     */
    public SingletonClustering(Graph<V, E> graph) {
        this.graph = requireUndirected(graph);
    }

    @Override
    public Clustering<V> getClustering() {
        final var clusters = graph.vertexSet().stream().
                map(Collections::singleton).
                collect(Collectors.toList());

        return new ClusteringImpl<>(Collections.unmodifiableList(clusters));
    }
}
