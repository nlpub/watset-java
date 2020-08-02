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

import java.util.Map;

/**
 * Useful implementations of {@link NodeWeighting}.
 */
public final class NodeWeightings {
    /**
     * A static factory method providing a convenient way to create an instance of {@link LabelNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@link LabelNodeWeighting}
     */
    public static <V, E> NodeWeighting<V, E> label() {
        return new LabelNodeWeighting<>();
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@link TopNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@link TopNodeWeighting}
     */
    public static <V, E> NodeWeighting<V, E> top() {
        return new TopNodeWeighting<>();
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@link LogNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@link LogNodeWeighting}
     */
    public static <V, E> NodeWeighting<V, E> log() {
        return new LogNodeWeighting<>();
    }

    /**
     * A static factory method providing a convenient way to create an instance of {@link LinearNodeWeighting}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return an instance of {@link LinearNodeWeighting}
     */
    public static <V, E> NodeWeighting<V, E> linear() {
        return new LinearNodeWeighting<>();
    }

    /**
     * A trivial and not particularly useful node weighting approach that
     * assigns the current node label as the weight.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class LabelNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return labels.get(node);
        }
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class TopNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor));
        }
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood
     * divided by the logarithm of the neighbor node degree.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class LogNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor)) / StrictMath.log1p(graph.degreeOf(neighbor));
        }
    }

    /**
     * The node weighting approach that chooses the label with the highest total edge weight in the neighborhood
     * divided by the neighbor node degree.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class LinearNodeWeighting<V, E> implements NodeWeighting<V, E> {
        @Override
        public double apply(Graph<V, E> graph, Map<V, Integer> labels, V node, V neighbor) {
            return graph.getEdgeWeight(graph.getEdge(node, neighbor)) / graph.degreeOf(neighbor);
        }
    }
}
