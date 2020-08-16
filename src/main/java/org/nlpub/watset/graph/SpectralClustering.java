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

package org.nlpub.watset.graph;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.util.VertexToIntegerMapping;
import org.nlpub.watset.util.Matrices;

import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.jgrapht.GraphTests.requireUndirected;

public class SpectralClustering<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link SpectralClustering}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, SpectralClustering<V, E>> {
        private Clusterer<NodeEmbedding<V>> clusterer;
        private Integer k;

        public Builder<V, E> setClusterer(Clusterer<NodeEmbedding<V>> clusterer) {
            this.clusterer = clusterer;
            return this;
        }

        public Builder<V, E> setK(int k) {
            this.k = k;
            return this;
        }

        @Override
        public SpectralClustering<V, E> apply(Graph<V, E> graph) {
            return new SpectralClustering<>(graph, clusterer, requireNonNull(k, "k must be specified"));
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

    private final Graph<V, E> graph;
    private final Clusterer<NodeEmbedding<V>> clusterer;
    private final int k;
    private Clustering<V> clustering;

    public SpectralClustering(Graph<V, E> graph, Clusterer<NodeEmbedding<V>> clusterer, int k) {
        this.graph = requireUndirected(graph);
        this.clusterer = clusterer;
        this.k = k;
    }

    @Override
    public Clustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph, clusterer, k).compute();
        }

        return clustering;
    }

    public static class Implementation<V, E> {
        protected final Graph<V, E> graph;
        protected final Clusterer<NodeEmbedding<V>> clusterer;
        protected final int k;
        protected final VertexToIntegerMapping<V> mapping;
        protected final RealMatrix degree;
        protected final RealMatrix adjacency;
        protected final RealMatrix laplacian;

        public Implementation(Graph<V, E> graph, Clusterer<NodeEmbedding<V>> clusterer, int k) {
            this.graph = graph;
            this.clusterer = clusterer;
            this.k = k;
            this.mapping = Graphs.getVertexToIntegerMapping(graph);
            this.degree = Matrices.buildDegreeMatrix(graph, mapping);
            this.adjacency = Matrices.buildAdjacencyMatrix(graph, mapping, false);
            this.laplacian = Matrices.buildSymmetricLaplacian(degree, adjacency);
        }

        public Clustering<V> compute() {
            final var matrix = new EigenDecomposition(laplacian).getV().
                    getSubMatrix(0, graph.vertexSet().size() - 1, 0, k - 1);

            final var norms = Matrices.computeRowNorms(matrix);
            matrix.walkInOptimizedOrder(new Matrices.RowNormalizeVisitor(norms));

            final var points = mapping.getVertexMap().entrySet().stream().
                    map(e -> new NodeEmbedding<>(e.getKey(), matrix.getRow(e.getValue()))).
                    collect(Collectors.toList());

            final var clusters = clusterer.cluster(points);

            return new ClusteringImpl<>(clusters.stream().
                    map(cluster -> cluster.getPoints().stream().
                            map(NodeEmbedding::getNode).
                            collect(Collectors.toSet())).
                    collect(Collectors.toList()));
        }
    }

    public static class NodeEmbedding<V> extends DoublePoint {
        protected final V node;

        public NodeEmbedding(V node, double[] point) {
            super(point);
            this.node = node;
        }

        public V getNode() {
            return node;
        }
    }
}
