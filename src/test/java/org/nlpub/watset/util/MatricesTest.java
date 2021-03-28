/*
 * Copyright 2021 Dmitry Ustalov
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

import junit.framework.TestCase;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jgrapht.util.VertexToIntegerMapping;
import org.junit.Test;
import org.nlpub.watset.graph.Fixtures;

public class MatricesTest extends TestCase {
    public static final VertexToIntegerMapping<String> MCL_MAPPING = new VertexToIntegerMapping<>(Fixtures.MCL_GRAPH.vertexSet());

    public static final RealMatrix MCL_LAPLACIAN = MatrixUtils.createRealMatrix(new double[][]{
            {3, -1, -1, -1},
            {-1, 2, 0, -1},
            {-1, 0, 1, 0},
            {-1, -1, 0, 2}
    });

    public static final RealMatrix MCL_LAPLACIAN_SYM = MatrixUtils.createRealMatrix(new double[][]{
            {1, -0.4082, -0.5774, -0.4082},
            {-0.4082, 1, 0, -0.5},
            {-0.5774, 0, 1, 0},
            {-0.4082, -0.5, 0, 1}
    });

    @Test
    public void testLaplacian() {
        final var degree = Matrices.buildDegreeMatrix(Fixtures.MCL_GRAPH, MCL_MAPPING);
        final var adjacency = Matrices.buildAdjacencyMatrix(Fixtures.MCL_GRAPH, MCL_MAPPING, false);

        final var laplacian = degree.subtract(adjacency);
        assertEquals(MCL_LAPLACIAN, laplacian);

        final var laplacianSym = Matrices.buildSymmetricLaplacian(degree, adjacency);
        assertEquals(0, (MCL_LAPLACIAN_SYM.subtract(laplacianSym)).getNorm(), 1e-3);
    }
}
