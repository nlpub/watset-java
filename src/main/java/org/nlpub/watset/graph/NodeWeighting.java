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

    /**
     * A trivial and not particularly useful node weighting approach that
     * assigns the current node label as the weight.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    class LabelNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return labels.get(node);
        }
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@code LabelNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@code LabelNodeWeighting}
     */
    static <V, E> NodeWeighting<V, E> label() {
        return new LabelNodeWeighting<>();
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    class TopNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor));
        }
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@code TopNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@code TopNodeWeighting}
     */
    static <V, E> NodeWeighting<V, E> top() {
        return new TopNodeWeighting<>();
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood
     * divided by the logarithm of the neighbor node degree.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    class LogNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor)) / Math.log1p(graph.degreeOf(neighbor));
        }
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@code LogNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@code LogNodeWeighting}
     */
    static <V, E> NodeWeighting<V, E> log() {
        return new LogNodeWeighting<>();
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood
     * divided by the neighbor node degree.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    class LinearNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor)) / graph.degreeOf(neighbor);
        }
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@code LinearNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@code LinearNodeWeighting}
     */
    static <V, E> NodeWeighting<V, E> linear() {
        return new LinearNodeWeighting<>();
    }
}
