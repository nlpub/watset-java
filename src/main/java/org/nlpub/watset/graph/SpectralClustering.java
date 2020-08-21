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

import org.apache.commons.math3.ml.clustering.Clusterer;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.util.VertexToIntegerMapping;
import org.nlpub.watset.util.Matrices;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.jgrapht.GraphTests.requireUndirected;

/**
 * Spectral Clustering performs clustering of the graph's Spectral Embedding.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1109/34.868688">Shi &amp; Malik (IEEE PAMI 22:8)</a>
 * @see <a href="https://doi.org/10.1007/s11222-007-9033-z">von Luxburg (Statistics and Computing 17:4)</a>
 * @see <a href="https://scikit-learn.org/stable/modules/generated/sklearn.cluster.SpectralClustering.html">sklearn.cluster.SpectralClustering</a>
 */
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

        /**
         * Set the underlying clustering algorithm.
         *
         * @param clusterer the clustering algorithm
         * @return the builder
         */
        public Builder<V, E> setClusterer(Clusterer<NodeEmbedding<V>> clusterer) {
            this.clusterer = clusterer;
            return this;
        }

        /**
         * Set the number of clusters.
         *
         * @param k the number of clusters
         * @return the builder
         */
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

    /**
     * The graph.
     */
    private final Graph<V, E> graph;

    /**
     * The underlying clustering algorithm.
     */
    private final Clusterer<NodeEmbedding<V>> clusterer;

    /**
     * The number of clusters.
     */
    private final int k;

    /**
     * The cached clustering result.
     */
    private Clustering<V> clustering;

    /**
     * Create an instance of the Spectral Clustering algorithm.
     *
     * @param graph     the graph
     * @param clusterer the clustering algorithm
     * @param k         the number of clusters
     */
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

    /**
     * Actual implementation of Spectral Clustering.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    public static class Implementation<V, E> {
        /**
         * The underlying clustering algorithm.
         */
        protected final Clusterer<NodeEmbedding<V>> clusterer;

        /**
         * The mapping of graph nodes to the columns of the graph Laplacian.
         */
        protected final VertexToIntegerMapping<V> mapping;

        /**
         * The node embeddings.
         */
        protected final List<NodeEmbedding<V>> embeddings;

        /**
         * Create an instance of the Spectral Clustering algorithm implementation.
         *
         * @param graph     the graph
         * @param clusterer the clustering algorithm
         * @param k         the number of clusters
         */
        public Implementation(Graph<V, E> graph, Clusterer<NodeEmbedding<V>> clusterer, int k) {
            this.clusterer = clusterer;
            this.mapping = Graphs.getVertexToIntegerMapping(graph);
            this.embeddings = Matrices.computeSpectralEmbedding(graph, mapping, k);
        }

        /**
         * Perform clustering with Spectral Clustering.
         *
         * @return the clustering
         */
        public Clustering<V> compute() {
            final var clusters = clusterer.cluster(embeddings);

            return new ClusteringImpl<>(clusters.stream().
                    map(cluster -> cluster.getPoints().stream().
                            map(NodeEmbedding::get).
                            collect(Collectors.toSet())).
                    collect(Collectors.toList()));
        }
    }
}
