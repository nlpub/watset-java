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

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
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
        final var matrix = MatrixUtils.createRealIdentityMatrix(graph.vertexSet().size());

        for (final var entry : mapping.getVertexMap().entrySet()) {
            final int i = entry.getValue();
            matrix.setEntry(i, i, graph.degreeOf(entry.getKey()));
        }

        return matrix;
    }

    public static RealMatrix buildSymmetricLaplacian(RealMatrix degree, RealMatrix adjacency) {
        final var eigen = new EigenDecomposition(degree);
        final var sqrt = eigen.getSquareRoot();
        final var laplacian = degree.subtract(adjacency);
        return sqrt.multiply(laplacian).multiply(sqrt);
    }
}
