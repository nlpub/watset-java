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

package org.nlpub.watset.eval;

import org.jgrapht.Graph;
import org.nlpub.watset.graph.Clustering;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A clustering algorithm performance measurement class.
 * <p>
 * Given a clustering algorithm provider and a graph, it measures the running time of the clustering
 * algorithm on every input graph.
 * <p>
 * Before recording the measurements, it <em>warms up</em> by running exactly the same operation to
 * leverage the influence of JIT and other VM optimizations.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see Clustering
 */
public class Measurer<V, E> {
    private static final Logger logger = Logger.getLogger(Measurer.class.getSimpleName());

    /**
     * The default number of repetitions.
     */
    final static public int REPETITIONS = 10;

    /**
     * The default number of warmup runs kept off-record before {@link #REPETITIONS}.
     */
    final static public int WARMUP = 5;

    private final Function<Graph<V, E>, Clustering<V>> provider;
    private final int repetitions, warmup;
    private final Graph<V, E> graph;
    private long[] durations;
    private int[] clusters;

    /**
     * Create an instance of {@code Measurer}.
     *
     * @param provider    the clustering algorithm provider
     * @param graph       the graph
     * @param repetitions the number of repetitions
     * @param warmup      the number of off-record repetitions
     */
    public Measurer(Function<Graph<V, E>, Clustering<V>> provider, Graph<V, E> graph, int repetitions, int warmup) {
        this.provider = provider;
        this.repetitions = repetitions;
        this.warmup = warmup;
        this.graph = graph;
    }

    /**
     * Create an instance of {@code Measurer}.
     *
     * @param provider the clustering algorithm provider
     * @param graph    the graph
     */
    public Measurer(Function<Graph<V, E>, Clustering<V>> provider, Graph<V, E> graph) {
        this(provider, graph, REPETITIONS, WARMUP);
    }

    /**
     * Return the input graph.
     *
     * @return the graph
     */
    public Graph<V, E> getGraph() {
        return graph;
    }

    /**
     * Return the list of the measured graph clustering durations in milliseconds.
     *
     * @return the list of durations
     */
    public List<Long> getDurations() {
        return Arrays.stream(durations).boxed().collect(Collectors.toList());
    }

    /**
     * Return the list of the measured number of clusters.
     *
     * @return the list of cluster sizes
     */
    public List<Integer> getClusters() {
        return Arrays.stream(clusters).boxed().collect(Collectors.toList());
    }

    /**
     * Perform the measurement. First, {@code warmup} iterations are performed off-record.
     * Then, the actual {@code repetitions} are performed to measure the performance of the clustering algorithm.
     */
    public void run() {
        System.gc();

        logger.log(Level.INFO, "Evaluating a graph with {0} node(s).", graph.vertexSet().size());

        durations = new long[repetitions];
        clusters = new int[repetitions];

        for (int i = -warmup; i < repetitions; i++) {
            final Clustering<V> clustering = provider.apply(graph);

            final Duration duration = measure(clustering);

            if (i >= 0) {
                durations[i] = duration.toMillis();
                clusters[i] = clustering.getClusters().size();
            }
        }

        logger.info("Evaluation complete.");
    }

    private Duration measure(Clustering<V> clustering) {
        final Instant start = Instant.now();
        clustering.fit();
        final Instant end = Instant.now();
        return Duration.between(start, end);
    }
}
