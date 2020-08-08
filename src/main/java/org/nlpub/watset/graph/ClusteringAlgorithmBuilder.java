/*
 * Copyright 2020 Dmitry Ustalov
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

import java.util.function.Function;

/**
 * A builder for constructing the {@link ClusteringAlgorithm} instances.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @param <C> the type of clustering algorithm
 */
public interface ClusteringAlgorithmBuilder<V, E, C extends ClusteringAlgorithm<V>> {
    /**
     * Construct an instance of the clustering algorithm with the parameters specified in the builder.
     *
     * @param graph the graph
     * @return a fully-configured clustering algorithm
     */
    @SuppressWarnings("unused")
    C build(Graph<V, E> graph);

    /**
     * Construct a factory function that sets up the algorithm for the given graph.
     *
     * @return a factory function that sets up the algorithm for the given graph
     */
    Function<Graph<V, E>, ClusteringAlgorithm<V>> provider();
}
