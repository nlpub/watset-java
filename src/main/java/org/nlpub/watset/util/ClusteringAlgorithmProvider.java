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

package org.nlpub.watset.util;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.nlpub.watset.graph.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A utility class that creates instances of the graph clustering algorithms.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class ClusteringAlgorithmProvider<V, E> implements ClusteringAlgorithmBuilder<V, E, ClusteringAlgorithm<V>> {
    /**
     * Clustering algorithms that {@link ClusteringAlgorithmProvider} knows how to provide.
     */
    public enum ProvidingAlgorithm {
        /**
         * Label for {@link EmptyClustering}.
         */
        EMPTY,

        /**
         * Label for {@link TogetherClustering}.
         */
        TOGETHER,

        /**
         * Label for {@link SingletonClustering}.
         */
        SINGLETON,

        /**
         * Label for {@link ComponentsClustering}.
         */
        COMPONENTS,

        /**
         * Label for {@link KSpanningTreeClustering}.
         */
        K_SPANNING_TREE,

        /**
         * Label for {@link SpectralClustering}.
         */
        SPECTRAL,

        /**
         * Label for {@link ChineseWhispers}.
         */
        CHINESE_WHISPERS,

        /**
         * Label for {@link MarkovClustering}.
         */
        MARKOV_CLUSTERING,

        /**
         * Label for {@link MarkovClusteringExternal}.
         */
        MARKOV_CLUSTERING_EXTERNAL,

        /**
         * Label for {@link MaxMax}.
         */
        MAXMAX,
    }

    private final ProvidingAlgorithm algorithm;
    private final Map<String, String> params;
    private final NodeWeighting<V, E> weighting;
    private final JDKRandomGenerator random;

    /**
     * Create an instance of this utility class.
     *
     * @param algorithm the algorithm identifier
     * @param params    the parameter map for the algorithm
     * @param random    the random number generator
     */
    public ClusteringAlgorithmProvider(String algorithm, Map<String, String> params, JDKRandomGenerator random) {
        this.algorithm = ProvidingAlgorithm.valueOf(normalize(requireNonNull(algorithm, "algorithm is not specified")));
        this.params = requireNonNullElse(params, Collections.emptyMap());
        this.weighting = NodeWeightings.parse(params.get("mode"));
        this.random = requireNonNullElse(random, new JDKRandomGenerator());
    }

    @Override
    public ClusteringAlgorithm<V> apply(Graph<V, E> graph) {
        switch (algorithm) {
            case EMPTY:
                return EmptyClustering.<V, E>builder().apply(graph);
            case TOGETHER:
                return TogetherClustering.<V, E>builder().apply(graph);
            case SINGLETON:
                return SingletonClustering.<V, E>builder().apply(graph);
            case COMPONENTS:
                return ComponentsClustering.<V, E>builder().apply(graph);
            case K_SPANNING_TREE:
                final int kst = Integer.parseInt(requireNonNull(params.get("k"), "k must be specified"));
                return new KSpanningTreeClustering<>(graph, kst);
            case SPECTRAL:
                final int kSpectral = Integer.parseInt(requireNonNull(params.get("k"), "k must be specified"));
                final var clusterer = new KMeansPlusPlusClusterer<NodeEmbedding<V>>(kSpectral, -1, new EuclideanDistance(), random);
                final int numTrials = params.containsKey("n") ? Integer.parseInt(params.get("n")) : 10;
                final var metaClusterer = new MultiKMeansPlusPlusClusterer<>(clusterer, numTrials);
                return SpectralClustering.<V, E>builder().setClusterer(metaClusterer).setK(kSpectral).apply(graph);
            case CHINESE_WHISPERS:
                return ChineseWhispers.<V, E>builder().setWeighting(weighting).setRandom(random).apply(graph);
            case MARKOV_CLUSTERING:
                final var mcl = MarkovClustering.<V, E>builder();

                if (params.containsKey("e")) mcl.setE(Integer.parseInt(params.get("e")));
                if (params.containsKey("r")) mcl.setR(Double.parseDouble(params.get("r")));

                return mcl.apply(graph);
            case MARKOV_CLUSTERING_EXTERNAL:
                final var mclOfficial = MarkovClusteringExternal.<V, E>builder().
                        setPath(Path.of(params.get("bin"))).
                        setThreads(Runtime.getRuntime().availableProcessors());

                if (params.containsKey("r")) mclOfficial.setR(Double.parseDouble(params.get("r")));

                return mclOfficial.apply(graph);
            case MAXMAX:
                return MaxMax.<V, E>builder().apply(graph);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * Normalize the name of the requested algorithm.
     *
     * @param algorithm the algorithm
     * @return the normalized name
     */
    protected String normalize(String algorithm) {
        return algorithm.toUpperCase(Locale.ROOT).
                replaceAll("-", "_").
                replaceAll("KST", ProvidingAlgorithm.K_SPANNING_TREE.name()).
                replaceAll("CW", ProvidingAlgorithm.CHINESE_WHISPERS.name()).
                replaceAll("MCL_BIN", ProvidingAlgorithm.MARKOV_CLUSTERING_EXTERNAL.name()).
                replaceAll("MCL", ProvidingAlgorithm.CHINESE_WHISPERS.name());
    }
}
