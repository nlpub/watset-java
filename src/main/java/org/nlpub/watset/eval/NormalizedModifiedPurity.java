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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

/**
 * Normalized modified purity evaluation measure for overlapping clustering.
 * <p>
 * Please be especially careful with the {@code hashCode} and {@code equals} methods of the elements.
 *
 * @param <V> the type of cluster elements
 * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">Evaluation of clustering</a>
 * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 */
public class NormalizedModifiedPurity<V> {
    /**
     * Transform a collection of clusters into a collection of weighted cluster elements.
     *
     * @param clusters the collection of clusters
     * @param <V>      the type of cluster elements
     * @return a collection of weighted cluster elements
     */
    public static <V> Collection<Map<V, Double>> transform(Collection<Collection<V>> clusters) {
        return clusters.stream().
                map(cluster -> cluster.stream().collect(Collectors.groupingBy(identity(), Collectors.reducing(0d, e -> 1d, Double::sum)))).
                collect(Collectors.toList());
    }

    /**
     * Normalize weights of the cluster elements to allow using normalized (modified) purity.
     *
     * @param clusters the collection of clusters
     * @param <V>      the type of cluster elements
     * @return a collection of weight-normalized clusters
     */
    public static <V> Collection<Map<V, Double>> normalize(Collection<Map<V, Double>> clusters) {
        final Map<V, Double> counter = new HashMap<>();

        clusters.stream().
                flatMap(cluster -> cluster.entrySet().stream()).
                forEach(entry -> counter.put(entry.getKey(), counter.getOrDefault(entry.getKey(), 0d) + entry.getValue()));

        final Collection<Map<V, Double>> normalized = clusters.stream().map(cluster -> {
            final var normalizedCluster = cluster.entrySet().stream().
                    collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / counter.get(entry.getKey())));

            if (cluster.size() != normalizedCluster.size()) throw new IllegalArgumentException("Cluster size changed");

            return normalizedCluster;
        }).collect(Collectors.toList());

        if (clusters.size() != normalized.size()) throw new IllegalArgumentException("Collection size changed");

        return normalized;
    }

    /**
     * Compute a precision and recall using purity and inverse purity, correspondingly.
     *
     * @param precision the purity
     * @param recall    the inverse purity
     * @param clusters  the collection of the clusters to evaluate
     * @param classes   the collection of the gold standard clusters
     * @param <V>       the type of cluster elements
     * @return precision and recalled wrapped in an instance of {@link PrecisionRecall}
     */
    public static <V> PrecisionRecall evaluate(NormalizedModifiedPurity<V> precision, NormalizedModifiedPurity<V> recall, Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes) {
        final var nmPU = precision.purity(requireNonNull(clusters), requireNonNull(classes));
        final var niPU = recall.purity(classes, clusters);
        return new PrecisionRecall(nmPU, niPU);
    }

    final boolean normalized, modified;

    /**
     * Construct a normalized modified purity calculator.
     */
    public NormalizedModifiedPurity() {
        this(true, true);
    }

    /**
     * Construct a normalized modified purity calculator that allows turning normalized and/or modified options off.
     *
     * @param normalized normalized purity is on
     * @param modified   modified purity is on
     */
    public NormalizedModifiedPurity(boolean normalized, boolean modified) {
        this.normalized = normalized;
        this.modified = modified;
    }

    /**
     * Computes the (modified) purity of the given clusters as according
     * to the gold standard clustering, classes.
     *
     * @param clusters the collection of the clusters to evaluate
     * @param classes  the collection of the gold standard clusters
     * @return (modified) purity
     * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
     */
    public double purity(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes) {
        double denominator = clusters.stream().mapToInt(Map::size).sum();

        if (normalized) {
            denominator = clusters.parallelStream().
                    mapToDouble(cluster -> cluster.values().stream().mapToDouble(Double::doubleValue).sum()).
                    sum();
        }

        if (denominator == 0) return 0;

        final var numerator = clusters.parallelStream().mapToDouble(cluster -> score(cluster, classes)).sum();

        return numerator / denominator;
    }

    /**
     * Compute the (modified) cluster score on a defined collection of classes.
     *
     * @param cluster the cluster to evaluate
     * @param classes the collection of the gold standard clusters
     * @return cluster score
     */
    public double score(Map<V, Double> cluster, Collection<Map<V, Double>> classes) {
        return classes.stream().mapToDouble(klass -> delta(cluster, klass)).max().orElse(0);
    }

    /**
     * Compute the fuzzy overlap between two clusters, {@code cluster} and {@code klass}.
     * <p>
     * In case of modified purity the singleton clusters are ignored.
     *
     * @param cluster the first cluster
     * @param klass   the second cluster
     * @return cluster overlap measure
     * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
     */
    public double delta(Map<V, Double> cluster, Map<V, Double> klass) {
        if (modified && !(cluster.size() > 1)) return 0;

        final var intersection = new HashMap<>(cluster);

        intersection.keySet().retainAll(klass.keySet());

        if (intersection.isEmpty()) return 0;

        if (!normalized) return intersection.size();

        return intersection.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
