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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.util.VertexToIntegerMapping;
import org.nlpub.watset.util.Matrices;

import java.util.*;
import java.util.logging.Logger;

import static java.util.Objects.isNull;
import static org.jgrapht.GraphTests.requireUndirected;

/**
 * Na&iuml;ve implementation of the Markov Clustering (MCL) algorithm.
 * <p>
 * This implementation assumes processing of relatively small graphs due to the lack of pruning optimizations.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://hdl.handle.net/1874/848">van Dongen (2000)</a>
 * @see <a href="https://doi.org/10.1137/040608635">van Dongen (2008)</a>
 */
public class MarkovClustering<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link MarkovClustering}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, MarkovClustering<V, E>> {
        /**
         * The default value of the expansion parameter.
         */
        public static final int E = 2;

        /**
         * The default value of the inflation parameter.
         */
        public static final double R = 2;

        /**
         * The default number of Markov Clustering iterations.
         */
        public static final int ITERATIONS = 20;

        private int e = E;
        private double r = R;
        private int iterations = ITERATIONS;

        @Override
        public MarkovClustering<V, E> apply(Graph<V, E> graph) {
            return new MarkovClustering<>(graph, e, r, iterations);
        }

        /**
         * Set the expansion parameter.
         *
         * @param e the expansion parameter
         * @return the builder
         */
        public Builder<V, E> setE(int e) {
            this.e = e;
            return this;
        }

        /**
         * Set the inflation parameter.
         *
         * @param r the inflation parameter
         * @return the builder
         */
        public Builder<V, E> setR(double r) {
            this.r = r;
            return this;
        }

        /**
         * Set the maximal number of iterations.
         *
         * @param iterations the maximal number of iterations
         * @return the builder
         */
        public Builder<V, E> setIterations(int iterations) {
            this.iterations = iterations;
            return this;
        }
    }

    /**
     * Create a builder.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a builder
     */
    public static <V, E> Builder<V, E> builder() {
        return new Builder<>();
    }

    private static final Logger logger = Logger.getLogger(MarkovClustering.class.getSimpleName());

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
     * The maximal number of iterations.
     */
    protected final int iterations;

    /**
     * The cached clustering result.
     */
    protected Clustering<V> clustering;

    /**
     * Create an instance of the Markov Clustering algorithm.
     *
     * @param graph      the graph
     * @param e          the expansion parameter
     * @param r          the inflation parameter
     * @param iterations the maximal number of iterations
     */
    public MarkovClustering(Graph<V, E> graph, int e, double r, int iterations) {
        this.graph = requireUndirected(graph);
        this.e = e;
        this.r = r;
        this.iterations = iterations;
    }

    @Override
    public Clustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph, e, r, iterations).compute();
        }

        return clustering;
    }

    /**
     * Actual implementation of Markov Clustering.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    protected static class Implementation<V, E> {
        /**
         * The graph.
         */
        protected final Graph<V, E> graph;

        /**
         * The expansion parameter.
         */
        protected final int e;

        /**
         * The maximal number of iterations.
         */
        protected final int iterations;

        /**
         * The inflation visitor that raises each element of {@code matrix} to the power of {@code r}.
         */
        protected final InflateVisitor inflateVisitor;

        /**
         * The mapping of graph nodes to the columns of {@code matrix}.
         */
        protected final VertexToIntegerMapping<V> mapping;

        /**
         * The row matrix filled by ones.
         */
        protected final RealVector ones;

        /**
         * The stochastic matrix.
         */
        protected RealMatrix matrix;

        /**
         * Create an instance of the Markov Clustering algorithm implementation.
         *
         * @param graph      the graph
         * @param e          the expansion parameter
         * @param r          the inflation parameter
         * @param iterations the maximal number of iterations
         */
        public Implementation(Graph<V, E> graph, int e, double r, int iterations) {
            this.graph = graph;
            this.e = e;
            this.iterations = iterations;
            this.inflateVisitor = new InflateVisitor(r);
            this.mapping = Graphs.getVertexToIntegerMapping(graph);

            final var data = new double[graph.vertexSet().size()];
            Arrays.fill(data, 1);
            this.ones = new ArrayRealVector(data, false);
        }

        /**
         * Perform clustering with Markov Clustering.
         *
         * @return the clustering
         */
        public Clustering<V> compute() {
            if (graph.vertexSet().isEmpty()) {
                return new ClusteringImpl<>(Collections.emptyList());
            }

            matrix = Matrices.buildAdjacencyMatrix(graph, mapping, true);

            normalize();

            for (var i = 0; i < iterations; i++) {
                final var previous = matrix.copy();

                expand();
                inflate();

                if (matrix.equals(previous)) break;
            }

            final var clusters = new ArrayList<Set<V>>();

            for (var i = 0; i < matrix.getRowDimension(); i++) {
                final var cluster = new HashSet<V>();

                for (var j = 0; j < matrix.getColumnDimension(); j++) {
                    if (matrix.getEntry(i, j) > 0) cluster.add(mapping.getIndexList().get(j));
                }

                if (!cluster.isEmpty()) clusters.add(cluster);
            }

            return new ClusteringImpl<>(clusters);
        }


        /**
         * Normalize the matrix.
         */
        protected void normalize() {
            final var sums = matrix.preMultiply(ones);
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

    /**
     * Visitor that raises each element to the power of {@link MarkovClustering#r}.
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

    /**
     * Visitor that normalizes columns.
     */
    public static class NormalizeVisitor extends DefaultRealMatrixChangingVisitor {
        /**
         * The row sums.
         */
        private final RealVector sums;

        /**
         * Create an instance of the normalizer.
         *
         * @param sums the column vector containing row sums
         */
        public NormalizeVisitor(RealVector sums) {
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
}
