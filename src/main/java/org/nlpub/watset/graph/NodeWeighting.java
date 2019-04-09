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
 * Node weighting approach for Chinese Whispers.
 *
 * @param <V> node class.
 * @param <E> edge class.
 * @see ChineseWhispers
 */
@FunctionalInterface
public interface NodeWeighting<V, E> {
    double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor);

    static <V, E> NodeWeighting<V, E> label() {
        return (graph, labels, node, neighbor) -> labels.get(node);
    }

    static <V, E> NodeWeighting<V, E> top() {
        return (graph, labels, node, neighbor) -> graph.getEdgeWeight(graph.getEdge(node, neighbor));
    }

    static <V, E> NodeWeighting<V, E> log() {
        return (graph, labels, node, neighbor) -> graph.getEdgeWeight(graph.getEdge(node, neighbor)) / Math.log(1 + graph.degreeOf(neighbor));
    }

    static <V, E> NodeWeighting<V, E> lin() {
        return (graph, labels, node, neighbor) -> graph.getEdgeWeight(graph.getEdge(node, neighbor)) / graph.degreeOf(neighbor);
    }
}
