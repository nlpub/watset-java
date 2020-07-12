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
import org.nlpub.watset.util.Neighbors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.nlpub.watset.util.Maximizer.argmaxRandom;

/**
 * Implementation of the Chinese Whispers algorithm.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.3115/1654758.1654774">Biemann (TextGraphs-1)</a>
 */
public class ChineseWhispers<V, E> implements Clustering<V> {
    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param weighting the node weighting approach
     * @param <V>       the type of nodes in the graph
     * @param <E>       the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting) {
        return graph -> new ChineseWhispers<>(graph, weighting);
    }

    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param weighting  the node weighting approach
     * @param iterations the number of iterations
     * @param random     the random number generator
     * @param <V>        the type of nodes in the graph
     * @param <E>        the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(NodeWeighting<V, E> weighting, int iterations, Random random) {
        return graph -> new ChineseWhispers<>(graph, weighting, iterations, random);
    }

    /**
     * The default number of Chinese Whispers iterations.
     */
    public static final int ITERATIONS = 20;

    /**
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * The node weighting approach.
     */
    protected final NodeWeighting<V, E> weighting;

    /**
     * The number of iterations.
     */
    protected final int iterations;

    /**
     * The random number generator.
     */
    protected final Random random;

    /**
     * The mapping of nodes to labels.
     */
    protected Map<V, Integer> labels;

    /**
     * The number of actual algorithm iterations.
     */
    protected int steps;

    /**
     * Create an instance of the Chinese Whispers algorithm.
     *
     * @param graph      the graph
     * @param weighting  the node weighting approach
     * @param iterations the number of iterations
     * @param random     the random number generator
     */
    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting, int iterations, Random random) {
        this.graph = requireNonNull(graph);
        this.weighting = requireNonNull(weighting);
        this.iterations = iterations;
        this.random = requireNonNull(random);
    }

    /**
     * Create an instance of the Chinese Whispers algorithm.
     *
     * @param graph     the graph
     * @param weighting the node weighting approach
     */
    public ChineseWhispers(Graph<V, E> graph, NodeWeighting<V, E> weighting) {
        this(graph, weighting, ITERATIONS, new Random());
    }

    @Override
    public void fit() {
        final var nodes = new ArrayList<>(graph.vertexSet());

        labels = new HashMap<>(nodes.size());

        var i = 0;

        for (final var node : graph.vertexSet()) {
            labels.put(node, i++);
        }

        for (steps = 0; steps < iterations; steps++) {
            Collections.shuffle(nodes, random);

            if (step(nodes) == 0) break;
        }
    }

    /**
     * Perform one iteration of the algorithm.
     *
     * @param nodes the list of nodes
     * @return whether any label changed or not
     */
    protected int step(List<V> nodes) {
        var changed = 0;

        for (final var node : nodes) {
            final var scores = score(graph, labels, weighting, node);

            final var label = argmaxRandom(scores.entrySet().iterator(), Map.Entry::getValue, random);

            final int updated = label.isPresent() ? label.get().getKey() : labels.get(node);

            // labels.put() never returns null for a known node
            @SuppressWarnings("ConstantConditions") final int previous = labels.put(node, updated);

            if (previous != updated) {
                changed++;
            }
        }

        return changed;
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        requireNonNull(labels, "call fit() first");

        final var groups = labels.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue));

        final List<Collection<V>> clusters = new ArrayList<>(groups.size());

        for (final var cluster : groups.values()) {
            clusters.add(cluster.stream().map(Map.Entry::getKey).collect(Collectors.toSet()));
        }

        return clusters;
    }

    /**
     * Score the label weights in the given neighborhood graph, which is a subgraph of {@link #graph}.
     * This method sums the node weights corresponding to each label.
     *
     * @param graph     the neighborhood graph
     * @param labels    the map of graph nodes to their labels
     * @param weighting the node weighting approach
     * @param node      the target node
     * @return a mapping of labels to sums of their weights
     */
    protected Map<Integer, Double> score(Graph<V, E> graph, Map<V, Integer> labels, NodeWeighting<V, E> weighting, V node) {
        final var weights = new HashMap<Integer, Double>();

        final var neighbors = Neighbors.neighborIterator(graph, node);

        neighbors.forEachRemaining(neighbor -> {
            final int label = labels.get(neighbor);
            weights.merge(label, weighting.apply(graph, labels, node, neighbor), Double::sum);
        });

        return weights;
    }

    /**
     * Return the number of iterations specified in the constructor
     *
     * @return the number of iterations
     * @see #ChineseWhispers(Graph, NodeWeighting, int, Random)
     */
    @SuppressWarnings("unused")
    public int getIterations() {
        return iterations;
    }

    /**
     * Return the number of iterations actually performed during {@link #fit()}.
     * Should be no larger than the value of {@link #getIterations()}.
     *
     * @return the number of iterations
     */
    @SuppressWarnings("unused")
    public int getSteps() {
        return steps;
    }
}
