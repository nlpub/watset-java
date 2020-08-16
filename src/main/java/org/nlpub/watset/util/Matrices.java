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

package org.nlpub.watset.util;

import org.apache.commons.math3.linear.*;
import org.jgrapht.Graph;
import org.jgrapht.util.VertexToIntegerMapping;

import java.util.Locale;
import java.util.logging.Logger;

public final class Matrices {
    private static final Logger logger = Logger.getLogger(Matrices.class.getSimpleName());

    private Matrices() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Construct an adjacency matrix for the given graph.
     * <p>
     * Note that the loops in the graph are ignored.
     *
     * @param graph    the graph
     * @param mapping  the mapping
     * @param addLoops should self-loops be added
     * @return an adjacency matrix
     */
    public static <V, E> RealMatrix buildAdjacencyMatrix(Graph<V, E> graph, VertexToIntegerMapping<V> mapping, boolean addLoops) {
        if (graph.vertexSet().size() > 2048) {
            logger.warning(() -> String.format(Locale.ROOT, "Graph is large: %d nodes.", graph.vertexSet().size()));
        }

        final var matrix = addLoops ?
                MatrixUtils.createRealIdentityMatrix(graph.vertexSet().size()) :
                MatrixUtils.createRealMatrix(graph.vertexSet().size(), graph.vertexSet().size());

        for (final var edge : graph.edgeSet()) {
            final int i = mapping.getVertexMap().get(graph.getEdgeSource(edge));
            final int j = mapping.getVertexMap().get(graph.getEdgeTarget(edge));

            if (i != j) {
                final var weight = graph.getEdgeWeight(edge);
                matrix.setEntry(i, j, weight);
                matrix.setEntry(j, i, weight);
            }
        }

        return matrix;
    }

    public static <V, E> RealMatrix buildDegreeMatrix(Graph<V, E> graph, VertexToIntegerMapping<V> mapping) {
        final var data = new double[graph.vertexSet().size()];

        for (final var entry : mapping.getVertexMap().entrySet()) {
            data[entry.getValue()] = graph.degreeOf(entry.getKey());
        }

        return MatrixUtils.createRealDiagonalMatrix(data);
    }

    public static RealMatrix buildSymmetricLaplacian(RealMatrix degree, RealMatrix adjacency) {
        final var eigen = new EigenDecomposition(degree);
        final var sqrt = eigen.getSquareRoot();
        final var laplacian = degree.subtract(adjacency);
        return sqrt.multiply(laplacian).multiply(sqrt);
    }

    public static RealVector computeRowNorms(RealMatrix matrix) {
        final var vector = new ArrayRealVector(matrix.getRowDimension());

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            vector.setEntry(i, matrix.getRowVector(i).getNorm());
        }

        return vector;
    }

    /**
     * Visitor that raises each element to the specified power.
     */
    public static class InflateVisitor extends DefaultRealMatrixChangingVisitor {
        /**
         * The inflation parameter.
         */
        private final double r;

        /**
         * Create an instance of the inflator.
         *
         * @param r the inflation parameter
         */
        public InflateVisitor(double r) {
            this.r = r;
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
            return StrictMath.pow(value, r);
        }
    }

    public static class ColumnSumVisitor extends DefaultRealMatrixPreservingVisitor {
        /**
         * The row sums.
         */
        private final RealVector sums;

        /**
         * Create an instance of the normalizer.
         *
         * @param sums the column vector containing row sums
         */
        public ColumnSumVisitor(RealVector sums) {
            this.sums = sums;
        }

        /**
         * Divide the value of a single element by the corresponding column of {@code sums}.
         *
         * @param row    row
         * @param column column
         * @param value  the value
         */
        @Override
        public void visit(int row, int column, double value) {
            sums.addToEntry(column, value);
        }
    }

    /**
     * Visitor that normalizes columns.
     */
    public static class ColumnNormalizeVisitor extends DefaultRealMatrixChangingVisitor {
        /**
         * The row sums.
         */
        private final RealVector sums;

        /**
         * Create an instance of the normalizer.
         *
         * @param sums the column vector containing row sums
         */
        public ColumnNormalizeVisitor(RealVector sums) {
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
            return value / sums.getEntry(column);
        }
    }

    public static class RowNormalizeVisitor extends DefaultRealMatrixChangingVisitor {
        /**
         * The column sums.
         */
        private final RealVector norms;

        /**
         * Create an instance of the normalizer.
         *
         * @param norms the column vector containing row sums
         */
        public RowNormalizeVisitor(RealVector norms) {
            this.norms = norms;
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
            return value / norms.getEntry(row);
        }
    }
}
