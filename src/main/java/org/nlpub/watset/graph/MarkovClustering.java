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

import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Na&iuml;ve implementation of the Markov Clustering (MCL) algorithm.
 * <p>
 * This implementation assumes processing of relatively small graphs due to the lack of pruning optimizations.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1137/040608635">van Dongen (2008)</a>
 */
public class MarkovClustering<V, E> implements Clustering<V> {
    /**
     * A factory function that sets up the algorithm for the given graph.
     *
     * @param e   the expansion parameter
     * @param r   the inflation parameter
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a factory function that sets up the algorithm for the given graph
     */
    @SuppressWarnings("unused")
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(int e, double r) {
        return graph -> new MarkovClustering<>(graph, e, r);
    }

    /**
     * Visitor that raises each element to the power of {@link MarkovClustering#r}.
     */
    public class InflateVisitor extends DefaultRealMatrixChangingVisitor {
        private InflateVisitor() {
        }

        /**
         * Raise the value of a single element to the power of {@code r}.
         *
         * @param row    row
         * @param column column
         * @param value  the value
         * @return the value raised to the power of {@code r}
         */
        @Override
        public double visit(int row, int column, double value) {
            return Math.pow(value, r);
        }
    }

    /**
     * Visitor that normalizes columns.
     */
    public static class NormalizeVisitor extends DefaultRealMatrixChangingVisitor {
        private final RealMatrix sums;

        /**
         * Create an instance of the normalizer.
         *
         * @param sums the column vector containing row sums
         */
        public NormalizeVisitor(RealMatrix sums) {
            this.sums = sums;
        }

        /**
         * Divide the value of a single element by the corresponding column of {@code sums}.
         *
         * @param row    row
         * @param column column
         * @param value  the value
         * @return the normalized value
         */
        @Override
        public double visit(int row, int column, double value) {
            return value / sums.getEntry(0, column);
        }
    }

    /**
     * The default number of Markov Clustering iterations.
     */
    public static final Integer ITERATIONS = 20;

    /**
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * The expansion parameter.
     */
    protected final int e;

    /**
     * The inflation parameter.
     */
    protected final double r;

    /**
     * The inflation visitor that raises each element of {@code matrix} to the power of {@code r}.
     */
    protected final InflateVisitor inflateVisitor;

    /**
     * The stochastic matrix.
     */
    protected RealMatrix matrix;

    /**
     * The row matrix filled by ones.
     */
    protected RealMatrix ones;

    /**
     * The mapping of graph nodes to the columns of {@code matrix}.
     */
    protected Map<V, Integer> index;

    /**
     * Create an instance of the Markov Clustering algorithm.
     *
     * @param graph the graph
     * @param e     the expansion parameter
     * @param r     the inflation parameter
     */
    public MarkovClustering(Graph<V, E> graph, int e, double r) {
        this.graph = requireNonNull(graph);
        this.e = e;
        this.r = r;
        this.inflateVisitor = new InflateVisitor();
    }

    @Override
    public void fit() {
        index = null;
        matrix = null;
        ones = null;

        if (graph.vertexSet().isEmpty()) return;

        index = buildIndex();
        matrix = buildMatrix(index);

        final double[] onesData = new double[matrix.getRowDimension()];
        Arrays.fill(onesData, 1);
        ones = MatrixUtils.createRowRealMatrix(onesData);

        normalize();

        for (int i = 0; i < ITERATIONS; i++) {
            final RealMatrix previous = matrix.copy();

            expand();
            inflate();

            if (matrix.equals(previous)) break;
        }
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        requireNonNull(index, "call fit() first");
        requireNonNull(matrix, "call fit() first");

        if (graph.vertexSet().isEmpty()) return Collections.emptySet();

        final Map<Integer, V> inverted = index.entrySet().stream().
                collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        final Set<Collection<V>> clusters = new HashSet<>();

        for (int r = 0; r < matrix.getRowDimension(); r++) {
            final Set<V> cluster = new HashSet<>();

            for (int c = 0; c < matrix.getColumnDimension(); c++) {
                if (matrix.getEntry(r, c) > 0) cluster.add(inverted.get(c));
            }

            if (!cluster.isEmpty()) clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * Index the nodes of the input graph.
     *
     * @return a node index
     */
    protected Map<V, Integer> buildIndex() {
        final Map<V, Integer> index = new HashMap<>();

        int i = 0;

        for (final V vertex : graph.vertexSet()) index.put(vertex, i++);

        return index;
    }

    /**
     * Construct an adjacency matrix for the given graph.
     * <p>
     * Note that the loops in the graph are ignored.
     *
     * @param index the node index
     * @return an adjacency matrix
     */
    protected RealMatrix buildMatrix(Map<V, Integer> index) {
        final RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(graph.vertexSet().size());

        for (final E edge : graph.edgeSet()) {
            final int i = index.get(graph.getEdgeSource(edge)), j = index.get(graph.getEdgeTarget(edge));

            if (i != j) {
                final double weight = graph.getEdgeWeight(edge);
                matrix.setEntry(i, j, weight);
                matrix.setEntry(j, i, weight);
            }
        }

        return matrix;
    }

    /**
     * Normalize the matrix.
     */
    protected void normalize() {
        final RealMatrix sums = ones.multiply(matrix);
        matrix.walkInOptimizedOrder(new NormalizeVisitor(sums));
    }

    /**
     * Perform the expansion step.
     */
    protected void expand() {
        matrix = matrix.power(e);
    }

    /**
     * Perform the inflation step.
     */
    protected void inflate() {
        normalize();
        matrix.walkInOptimizedOrder(inflateVisitor);
    }
}
