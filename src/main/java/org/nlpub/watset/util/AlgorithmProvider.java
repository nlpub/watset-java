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
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * A utility class that creates instances of the graph clustering algorithms.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class AlgorithmProvider<V, E> implements Function<Graph<V, E>, Clustering<V>> {
    /**
     * The default separator in parameter strings, expressed by the colon symbol.
     */
    public static final Pattern SEPARATOR = Pattern.compile(":");

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
        this.params = requireNonNull(params);
    }

    /**
     * Create an instance of this utility class.
     * <p>
     * This constructor parses the parameter string {@code params} using {@link #parseParams(String)}.
     *
     * @param algorithm the algorithm identifier
     * @param params    the parameter string for the algorithm
     */
    public AlgorithmProvider(String algorithm, String params) {
        this(algorithm, parseParams(params));
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
            case "lin":
            case "nolog": // We used this notation in many papers; kept for compatibility
                return NodeWeighting.linear();
            default:
                throw new IllegalArgumentException("Unknown mode is set.");
        }
    }

    /**
     * Parse the algorithm parameter string similarly to how HTTP query strings are parsed.
     * However, instead of {@code &} delimiter {@code :} is used.
     *
     * @param params the parameter string
     * @return the parsed parameter map
     */
    static Map<String, String> parseParams(String params) {
        if (isNull(params) || params.trim().isEmpty()) return Collections.emptyMap();

        return SEPARATOR.splitAsStream(params).
                map(s -> s.split("=", 2)).
                filter(pair -> pair.length == 2).
                collect(toMap(kv -> kv[0].toLowerCase(Locale.ROOT), kv -> kv[1]));
    }
}
