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

import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.Graph;
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
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider(int e, double r) {
        return graph -> new MarkovClustering<>(graph, e, r);
    }

    public static final Integer ITERATIONS = 20;

    protected final Graph<V, E> graph;
    protected final int e;
    protected final double r;
    protected RealMatrix matrix;
    protected Map<V, Integer> index;

    public MarkovClustering(Graph<V, E> graph, int e, double r) {
        this.graph = requireNonNull(graph);
        this.e = e;
        this.r = r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        index = null;
        matrix = null;

        if (graph.vertexSet().isEmpty()) return;

        index = buildIndex(graph);
        matrix = buildMatrix(graph, index);

        normalize(matrix);

        for (int i = 0; i < ITERATIONS; i++) {
            final RealMatrix previous = matrix.copy();

            expand(matrix);
            inflate(matrix);

            if (matrix.equals(previous)) break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Collection<V>> getClusters() {
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
     * Construct an adjacency sums for a given graph. Note that the loops are ignored.
     *
     * @param graph a graph.
     * @param index a node index for the graph.
     * @return an adjacency sums.
     */
    protected RealMatrix buildMatrix(Graph<V, E> graph, Map<V, Integer> index) {
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

    private class NormalizeVisitor extends DefaultRealMatrixChangingVisitor {
        private final RealMatrix sums;

        public NormalizeVisitor(RealMatrix sums) {
            this.sums = sums;
        }

        @Override
        public double visit(int row, int column, double value) {
            return value / sums.getEntry(0, column);
        }
    }

    protected void normalize(RealMatrix matrix) {
        final RealMatrix sums = createRowOnesRealMatrix(matrix.getRowDimension()).multiply(matrix);
        matrix.walkInOptimizedOrder(new NormalizeVisitor(sums));
    }

    protected void expand(RealMatrix matrix) {
        this.matrix = matrix.power(e);
    }

    private class InflateVisitor extends DefaultRealMatrixChangingVisitor {
        private final double r;

        public InflateVisitor(double r) {
            this.r = r;
        }

        @Override
        public double visit(int row, int column, double value) {
            return Math.pow(value, r);
        }
    }

    protected void inflate(RealMatrix matrix) {
        normalize(matrix);
        matrix.walkInOptimizedOrder(new InflateVisitor(r));
    }

    private RealMatrix createRowOnesRealMatrix(int n) {
        final double[] ones = new double[n];
        Arrays.fill(ones, 1);
        return MatrixUtils.createRowRealMatrix(ones);
    }
}
