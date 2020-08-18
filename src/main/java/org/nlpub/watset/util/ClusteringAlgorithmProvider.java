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
    private final String algorithm;
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
        this.algorithm = requireNonNull(algorithm, "algorithm is not specified");
        this.params = requireNonNullElse(params, Collections.emptyMap());
        this.weighting = NodeWeightings.parse(params.get("mode"));
        this.random = requireNonNullElse(random, new JDKRandomGenerator());
    }

    @Override
    public ClusteringAlgorithm<V> apply(Graph<V, E> graph) {
        switch (algorithm.toLowerCase(Locale.ROOT)) {
            case "empty":
                return EmptyClustering.<V, E>builder().apply(graph);
            case "together":
                return TogetherClustering.<V, E>builder().apply(graph);
            case "singleton":
                return SingletonClustering.<V, E>builder().apply(graph);
            case "components":
                return ComponentsClustering.<V, E>builder().apply(graph);
            case "kst":
                final int kst = Integer.parseInt(requireNonNull(params.get("k"), "k must be specified"));
                return new KSpanningTreeClustering<>(graph, kst);
            case "spectral":
                final int kSpectral = Integer.parseInt(requireNonNull(params.get("k"), "k must be specified"));
                final var clusterer = new KMeansPlusPlusClusterer<NodeEmbedding<V>>(kSpectral, -1, new EuclideanDistance(), random);
                final var metaClusterer = new MultiKMeansPlusPlusClusterer<>(clusterer, 10);
                return SpectralClustering.<V, E>builder().setClusterer(metaClusterer).setK(kSpectral).apply(graph);
            case "cw":
                return ChineseWhispers.<V, E>builder().setWeighting(weighting).setRandom(random).apply(graph);
            case "mcl":
                final var mcl = MarkovClustering.<V, E>builder();

                if (params.containsKey("e")) mcl.setE(Integer.parseInt(params.get("e")));
                if (params.containsKey("r")) mcl.setR(Double.parseDouble(params.get("r")));

                return mcl.apply(graph);
            case "mcl-bin":
                final var mclOfficial = MarkovClusteringExternal.<V, E>builder().
                        setPath(Path.of(params.get("bin"))).
                        setThreads(Runtime.getRuntime().availableProcessors());

                if (params.containsKey("r")) mclOfficial.setR(Double.parseDouble(params.get("r")));

                return mclOfficial.apply(graph);
            case "maxmax":
                return MaxMax.<V, E>builder().apply(graph);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }
}
