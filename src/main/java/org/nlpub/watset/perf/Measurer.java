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

package org.nlpub.watset.perf;

import org.jgrapht.Graph;
import org.nlpub.watset.graph.Clustering;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * This is a clustering algorithm performance evaluator. Given a clustering algorithm provider and
 * a set of graphs, it measures the running time of the clustering algorithm on every input graph.
 * Before recording the measurements, it <i>warms up</i> by running exactly the same operation to
 * leverage the influence of JIT and other VM optimizations.
 *
 * @param <V> node class.
 * @param <E> edge class.
 */
public class Measurer<V, E> implements Runnable {
    private static final Logger logger = Logger.getLogger(Measurer.class.getSimpleName());

    final static public int WARMUP = 15;
    final static public int REPETITIONS = 35;

    private final Function<Graph<V, E>, Clustering<V>> provider;
    private final int repetitions, warmup;
    private Set<Graph<V, E>> graphs;
    private Map<Graph<V, E>, List<Long>> observations;

    public Measurer(Function<Graph<V, E>, Clustering<V>> provider) {
        this(provider, REPETITIONS, WARMUP);
    }

    public Measurer(Function<Graph<V, E>, Clustering<V>> provider, int repetitions, int warmup) {
        this.provider = provider;
        this.repetitions = repetitions;
        this.warmup = warmup;
        this.graphs = new LinkedHashSet<>();
        this.observations = Collections.emptyMap();
    }

    public Set<Graph<V, E>> getGraphs() {
        return graphs;
    }

    public void setGraphs(Set<Graph<V, E>> graphs) {
        this.graphs = graphs;
    }

    public Map<Graph<V, E>, List<Long>> getObservations() {
        return observations;
    }

    public void run() {
        observations = new LinkedHashMap<>(graphs.size());

        for (final Graph<V, E> graph : graphs) {
            logger.info(String.format(Locale.ROOT, "Evaluating a graph with %d node(s).", graph.vertexSet().size()));

            final List<Long> measurements = new ArrayList<>(warmup + repetitions);

            for (int i = -warmup; i < repetitions; i++) {
                final Clustering<V> clustering = provider.apply(graph);
                final Duration duration = measure(clustering);
                if (i >= 0) measurements.add(i, duration.toMillis());
            }

            observations.put(graph, measurements);

            logger.info(String.format(Locale.ROOT, "Measurements done: %d out of %d.", observations.size(), graphs.size()));
        }
    }

    private Duration measure(@Nonnull Clustering<V> clustering) {
        final Instant start = Instant.now();
        clustering.run();
        final Instant end = Instant.now();
        System.gc();
        return Duration.between(start, end);
    }
}
