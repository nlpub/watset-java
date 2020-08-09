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

import static org.jgrapht.GraphTests.requireUndirected;

/**
 * A trivial clustering algorithm that puts every node together in a single large cluster.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class TogetherClustering<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link TogetherClustering}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, TogetherClustering<V, E>> {
        @Override
        public TogetherClustering<V, E> apply(Graph<V, E> graph) {
            return new TogetherClustering<>(graph);
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
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * Set up the trivial clustering algorithm that puts every node together in a single large cluster.
     *
     * @param graph the graph
     */
    public TogetherClustering(Graph<V, E> graph) {
        this.graph = requireUndirected(graph);
    }

    @Override
    public Clustering<V> getClustering() {
        return new ClusteringImpl<>(Collections.singletonList(graph.vertexSet()));
    }
}
