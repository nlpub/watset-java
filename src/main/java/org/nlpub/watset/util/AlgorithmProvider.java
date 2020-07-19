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

import org.jgrapht.Graph;
import org.nlpub.watset.graph.*;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * A utility class that creates instances of the graph clustering algorithms.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class AlgorithmProvider<V, E> implements Function<Graph<V, E>, Clustering<V>> {
    private static final Logger logger = Logger.getLogger(SimplifiedWatset.class.getSimpleName());

    private final String algorithm;
    private final Map<String, String> params;

    /**
     * Create an instance of this utility class.
     *
     * @param algorithm the algorithm identifier
     * @param params    the parameter map for the algorithm
     */
    public AlgorithmProvider(String algorithm, Map<String, String> params) {
        this.algorithm = requireNonNull(algorithm);
        this.params = isNull(params) ? Collections.emptyMap() : params;
    }

    @Override
    public Clustering<V> apply(Graph<V, E> graph) {
        switch (algorithm.toLowerCase(Locale.ROOT)) {
            case "empty":
                return new EmptyClustering<>();
            case "together":
                return new TogetherClustering<>(graph);
            case "singleton":
                return new SingletonClustering<>(graph);
            case "components":
                return new ComponentsClustering<>(graph);
            case "cw":
                final var weighting = parseChineseWhispersNodeWeighting();
                return new ChineseWhispers<>(graph, weighting);
            case "mcl":
            case "mcl-bin":
                final var e = Integer.parseInt(params.getOrDefault("e", "2"));
                final var r = Double.parseDouble(params.getOrDefault("r", "2"));
                if (algorithm.equalsIgnoreCase("mcl")) {
                    return new MarkovClustering<>(graph, e, r);
                } else {
                    return new MarkovClusteringBinaryRunner<>(graph, Paths.get(params.get("bin")), r, Runtime.getRuntime().availableProcessors());
                }
            case "maxmax":
                return new MaxMax<>(graph);
            default:
                throw new IllegalArgumentException("Unknown algorithm is set.");
        }
    }

    private NodeWeighting<V, E> parseChineseWhispersNodeWeighting() {
        switch (params.getOrDefault("mode", "top").toLowerCase(Locale.ROOT)) {
            case "label":
                return NodeWeighting.label();
            case "top":
                return NodeWeighting.top();
            case "log":
                return NodeWeighting.log();
            case "nolog": // We used this notation in many papers; kept for compatibility
                logger.warning("Please update your code: 'nolog' weighting is renamed to 'lin'.");
            case "lin":
                return NodeWeighting.linear();
            default:
                throw new IllegalArgumentException("Unknown mode is set.");
        }
    }
}
