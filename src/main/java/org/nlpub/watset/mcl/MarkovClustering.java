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

package org.nlpub.watset.mcl;

import org.jgrapht.Graph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Pow;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nlpub.watset.graph.Clustering;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This is an implementation of the Markov Clustering (MCL) algorithm.
 * It assumes processing of relatively small graphs due to the lack of
 * pruning optimizations.
 *
 * @param <V> vertices
 * @param <E> edges
 * @see <a href="https://micans.org/mcl/">van Dongen (2000)</a>
 */
public class MarkovClustering<V, E> implements Clustering<V> {
    public static final <V, E> Function<Graph<V, E>, Clustering<V>> provider(int e, double r) {
        return graph -> new MarkovClustering<>(graph, e, r);
    }

    public static final Integer ITERATIONS = 20;

    protected final Graph<V, E> graph;
    protected final int e;
    protected final double r;
    protected INDArray matrix;
    protected Map<V, Integer> index;

    public MarkovClustering(Graph<V, E> graph, int e, double r) {
        this.graph = requireNonNull(graph);
        this.e = e;
        this.r = r;
    }

    @Override
    public void run() {
        index = null;
        matrix = null;

        if (graph.vertexSet().isEmpty()) return;

        index = buildIndex(graph);
        matrix = buildMatrix(graph, index);

        normalize(matrix);

        for (int i = 0; i < ITERATIONS; i++) {
            final INDArray previous = matrix.unsafeDuplication();

            expand(matrix);
            inflate(matrix);

            if (matrix.equals(previous)) break;
        }
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        if (graph.vertexSet().isEmpty()) return Collections.emptySet();

        final Map<Integer, V> inverted = index.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final Set<Collection<V>> clusters = new HashSet<>();

        for (int r = 0; r < matrix.rows(); r++) {
            final INDArray row = matrix.getRow(r);

            final Set<V> cluster = new HashSet<>();

            for (int c = 0; c < row.length(); c++) {
                if (row.getDouble(c) > 0) cluster.add(inverted.get(c));
            }

            if (!cluster.isEmpty()) clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * Index the nodes of the input graph.
     *
     * @param graph an input graph.
     * @return a node index.
     */
    protected Map<V, Integer> buildIndex(Graph<V, E> graph) {
        final Map<V, Integer> index = new HashMap<>();

        int i = 0;

        for (final V vertex : graph.vertexSet()) index.put(vertex, i++);

        return index;
    }

    /**
     * Construct an adjacency matrix for a given graph. Note that the loops are ignored.
     *
     * @param graph a graph.
     * @param index a node index for the graph.
     * @return an adjacency matrix.
     */
    protected INDArray buildMatrix(Graph<V, E> graph, Map<V, Integer> index) {
        final INDArray matrix = Nd4j.eye(graph.vertexSet().size());

        for (final E edge : graph.edgeSet()) {
            final int i = index.get(graph.getEdgeSource(edge)), j = index.get(graph.getEdgeTarget(edge));

            if (i != j) {
                final double weight = graph.getEdgeWeight(edge);
                matrix.put(i, j, weight);
                matrix.put(j, i, weight);
            }
        }

        return matrix;
    }

    protected void normalize(INDArray matrix) {
        final INDArray sums = matrix.sum(0);
        matrix.diviRowVector(sums);
    }

    protected void expand(INDArray matrix) {
        Transforms.mpow(matrix, e, false);
    }

    protected void inflate(INDArray matrix) {
        Nd4j.getExecutioner().exec(new Pow(matrix, r));
        normalize(matrix);
    }
}
