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
import org.jgrapht.Graphs;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Pow;
import org.nd4j.linalg.factory.Nd4j;
import org.nlpub.watset.graph.Clustering;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This is a non-optimized implementation of the Markov Clustering algorithm.
 * <p>
 * In fact, this is a translation to Java of the Python source:
 * https://github.com/lucagiovagnoli/Markov_clustering-Graph_API.
 *
 * @param <V> vertices
 * @param <E> edges
 * @see <a href="https://micans.org/mcl/">van Dongen (2000)</a>
 */
public class MarkovClustering<V, E> implements Clustering<V> {
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
        this.index = null;
        this.matrix = null;

        this.index = buildNodeIndex(graph);
        this.matrix = buildAdjacencyMatrix(graph, index);

        normalizeColumns(matrix);

        for (int i = 0; i < ITERATIONS; i++) {
            final INDArray previous = matrix.dup();

            matrix = powerStep(matrix);
            matrix = inflationStep(matrix);

            if (matrix.sub(previous).sumNumber().doubleValue() == 0d) break;
        }
    }

    @Override
    public Collection<Collection<V>> getClusters() {
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

    protected Map<V, Integer> buildNodeIndex(Graph<V, E> graph) {
        final Map<V, Integer> index = new HashMap<>();

        int i = 0;

        for (final V vertex : graph.vertexSet()) {
            index.put(vertex, i++);
        }

        return index;
    }

    protected INDArray buildAdjacencyMatrix(Graph<V, E> graph, Map<V, Integer> index) {
        final double[][] matrix = new double[graph.vertexSet().size()][graph.vertexSet().size()];

        for (Map.Entry<V, Integer> entry : index.entrySet()) {
            for (final E edge : graph.edgesOf(entry.getKey())) {
                final V neighbor = Graphs.getOppositeVertex(graph, edge, entry.getKey());
                matrix[entry.getValue()][index.get(neighbor)] = graph.getEdgeWeight(edge);
            }
        }

        for (int r = 0; r < matrix.length; r++) matrix[r][r] = 1;

        return Nd4j.create(matrix);
    }

    protected void normalizeColumns(INDArray matrix) {
        final INDArray sums = matrix.sum(0);

        for (int c = 0; c < matrix.shape()[1]; c++) {
            final double sum = sums.getDouble(c);

            if (sum == 0) continue;

            for (int r = 0; r < matrix.shape()[0]; r++) {
                matrix.put(r, c, matrix.getDouble(r, c) / sum);
            }
        }
    }

    protected INDArray powerStep(INDArray matrix) {
        INDArray tmp = matrix;

        for (int i = 0; i < e; i++) {
            tmp = tmp.mmul(tmp);
        }

        return tmp;
    }

    protected INDArray inflationStep(INDArray matrix) {
        final INDArray tmp = Nd4j.getExecutioner().execAndReturn(new Pow(matrix, r));
        normalizeColumns(tmp);
        return tmp;
    }
}
