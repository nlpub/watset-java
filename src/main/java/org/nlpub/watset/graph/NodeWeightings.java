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

import java.lang.System.Logger.Level;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNullElse;

/**
 * Useful implementations of {@link NodeWeighting}.
 */
public final class NodeWeightings {
    /**
     * Weighting modes.
     */
    public enum WeightingMode {
        /**
         * Weighting mode {@code label}.
         *
         * @see LabelNodeWeighting
         */
        LABEL,

        /**
         * Weighting mode {@code top}.
         *
         * @see TopNodeWeighting
         */
        TOP,

        /**
         * Weighting mode {@code log}.
         *
         * @see LogNodeWeighting
         */
        LOG,

        /**
         * Weighting mode {@code lin}.
         *
         * @see LinearNodeWeighting
         * @deprecated Replaced with {@code LIN}
         */
        @SuppressWarnings("DeprecatedIsStillUsed") @Deprecated NOLOG,

        /**
         * Weighting mode {@code lin}.
         *
         * @see LinearNodeWeighting
         */
        LIN
    }

    private static final System.Logger logger = System.getLogger(NodeWeightings.class.getSimpleName());

    private NodeWeightings() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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

    /**
     * Construct a node weighting instance corresponding to the given {@code mode}.
     *
     * @param mode the mode identifier
     * @param <V>  the type of nodes in the graph
     * @param <E>  the type of edges in the graph
     * @return an instance of {@link NodeWeighting}
     * @see WeightingMode
     */
    public static <V, E> NodeWeighting<V, E> parse(String mode) {
        final var parsed = WeightingMode.valueOf(requireNonNullElse(mode, WeightingMode.TOP.name()).toUpperCase(Locale.ROOT));

        switch (parsed) {
            case LABEL:
                return NodeWeightings.label();
            case TOP:
                return NodeWeightings.top();
            case LOG:
                return NodeWeightings.log();
            case NOLOG: // We used this notation in many papers; kept for compatibility
                logger.log(Level.WARNING, "Please update your code: 'nolog' weighting is renamed to 'lin'.");
                return NodeWeightings.linear();
            case LIN:
                return NodeWeightings.linear();
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }
}
