/*
 * Copyright 2018 Dmitry Ustalov
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

package org.nlpub.watset.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Please be especially careful with the hashCode and equals methods of the cluster elements.
 *
 * @param <V> cluster element type.
 */
public class NormalizedModifiedPurity<V> implements Supplier<PrecisionRecall> {
    public static <V> Collection<Map<V, Double>> transform(Collection<Collection<V>> clusters) {
        return clusters.stream().
                map(cluster -> cluster.stream().
                        collect(groupingBy(identity(), reducing(0d, e -> 1d, Double::sum)))).
                collect(toList());
    }

    private final Collection<Map<V, Double>> clusters;
    private final Collection<Map<V, Double>> classes;
    private final boolean fuzzy;

    public NormalizedModifiedPurity(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes, boolean fuzzy) {
        this.fuzzy = fuzzy;
        this.clusters = fuzzy ? normalize(requireNonNull(clusters)) : requireNonNull(clusters);
        this.classes = fuzzy ? normalize(requireNonNull(classes)) : requireNonNull(classes);
    }

    public NormalizedModifiedPurity(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes) {
        this(clusters, classes, true);
    }

    @Override
    public PrecisionRecall get() {
        final double normalizedModifiedPurity = purity(clusters, classes, true);
        final double normalizedInversePurity = purity(classes, clusters, false);
        return new PrecisionRecall(normalizedModifiedPurity, normalizedInversePurity);
    }

    private double purity(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes, boolean modified) {
        double denominator = clusters.stream().mapToInt(Map::size).sum();

        if (fuzzy) {
            denominator = clusters.stream().
                    mapToDouble(cluster -> cluster.values().stream().mapToDouble(a -> a).sum()).
                    sum();
        }

        if (denominator == 0) return 0;

        double numerator = clusters.stream().
                mapToDouble(cluster -> classes.stream().
                        mapToDouble(klass -> delta(cluster, klass, modified)).max().orElse(0)).
                sum();

        return numerator / denominator;
    }

    private double delta(Map<V, Double> cluster, Map<V, Double> klass, boolean modified) {
        if (modified && !(cluster.size() > 1)) return 0;

        final Map<V, Double> intersection = new HashMap<>(cluster);

        intersection.keySet().retainAll(klass.keySet());

        if (intersection.isEmpty()) return 0;

        if (!fuzzy) return intersection.size();

        return intersection.values().stream().mapToDouble(a -> a).sum();
    }

    private Collection<Map<V, Double>> normalize(Collection<Map<V, Double>> clusters) {
        final Map<V, Double> counter = new HashMap<>();

        clusters.stream().flatMap(cluster -> cluster.entrySet().stream()).
                forEach(entry -> counter.put(entry.getKey(), counter.getOrDefault(entry.getKey(), 0d) + entry.getValue()));

        final Collection<Map<V, Double>> normalized = clusters.stream().map(cluster -> {
            final Map<V, Double> normalizedCluster = cluster.entrySet().stream().
                    collect(toMap(Map.Entry::getKey, entry -> entry.getValue() / counter.get(entry.getKey())));

            if (cluster.size() != normalizedCluster.size()) throw new IllegalArgumentException("Cluster size changed");

            return normalizedCluster;
        }).collect(toList());

        if (clusters.size() != normalized.size()) throw new IllegalArgumentException("Collection size changed");

        return normalized;
    }
}
