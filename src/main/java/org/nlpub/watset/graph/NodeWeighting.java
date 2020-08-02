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

import java.util.Map;

/**
 * Node weighting for Chinese Whispers.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see ChineseWhispers
 */
@FunctionalInterface
public interface NodeWeighting<V, E> {
    /**
     * Compute the weight of the node in the neighborhood graph.
     *
     * @param graph    the neighborhood graph
     * @param labels   the map of graph nodes to their labels
     * @param node     the target node
     * @param neighbor the neighboring node
     * @return the weight of the node in the neighborhood
     */
    double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor);
}
