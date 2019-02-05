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

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * An implementation of the normalized modified purity evaluation measure for overlapping clustering.
 * <p>
 * Please be especially careful with the hashCode and equals methods of the cluster elements.
 *
 * @param <V> a cluster element type.
 * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">Evaluation of clustering</a>
 * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
 * @see <a href="https://aclweb.org/anthology/P18-2010">Ustalov et al. (ACL 2018)</a>
 */
public class NormalizedModifiedPurity<V> {
    /**
     * Transforms a collection of clusters into a collection of weighted cluster elements.
     *
     * @param clusters a collection of clusters.
     * @param <V>      a cluster element type.
     * @return a collection of weighted cluster elements.
     */
    public static <V> Collection<Map<V, Double>> transform(Collection<Collection<V>> clusters) {
        return clusters.stream().
                map(cluster -> cluster.stream().
                        collect(groupingBy(identity(), reducing(0d, e -> 1d, Double::sum)))).
                collect(toList());
    }

    /**
     * Normalizes clusters to allow using normalized (modified) purity.
     *
     * @param clusters a collection of clusters.
     * @param <V>      a cluster element type.
     * @return a collection of normalized clusters.
     */
    public static <V> Collection<Map<V, Double>> normalize(Collection<Map<V, Double>> clusters) {
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

    final boolean normalized, modified;

    /**
     * Constructs a normalized modified purity calculator.
     */
    public NormalizedModifiedPurity() {
        this(true, true);
    }

    /**
     * Constructs a normalized modified purity calculator that allows
     * turning normalized and/or modified options off.
     *
     * @param normalized normalized purity is on.
     * @param modified   modified purity is on.
     */
    public NormalizedModifiedPurity(boolean normalized, boolean modified) {
        this.normalized = normalized;
        this.modified = modified;
    }

    /**
     * Computes a purity (nmPU), inverse purity (niPU) and F-score.
     *
     * @param clusters a collection of clusters.
     * @param classes  a collection of gold standard clusters.
     * @return precision-recall report.
     */
    public PrecisionRecall evaluate(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes) {
        final double nmPU = purity(requireNonNull(clusters), requireNonNull(classes), modified);
        final double niPU = purity(classes, clusters, false);
        return new PrecisionRecall(nmPU, niPU);
    }

    /**
     * Computes the (modified) purity of the given clusters as according
     * to the gold standard clustering, classes.
     *
     * @param clusters clustering
     * @param classes  gold clustering
     * @param modified whether to use a modified purity
     * @return (modified) purity
     * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
     */
    public double purity(Collection<Map<V, Double>> clusters, Collection<Map<V, Double>> classes, boolean modified) {
        double denominator = clusters.stream().mapToInt(Map::size).sum();

        if (normalized) {
            denominator = clusters.parallelStream().
                    mapToDouble(cluster -> cluster.values().stream().mapToDouble(a -> a).sum()).
                    sum();
        }

        if (denominator == 0) return 0;

        final double numerator = clusters.parallelStream().
                mapToDouble(cluster -> score(cluster, classes, modified)).sum();

        return numerator / denominator;
    }

    /**
     * Computes the (modified) cluster score on a defined collection of classes.
     *
     * @param cluster  a cluster.
     * @param classes  a collection of classes.
     * @param modified whether to use a modified purity
     * @return cluster score
     */
    public double score(Map<V, Double> cluster, Collection<Map<V, Double>> classes, boolean modified) {
        return classes.stream().mapToDouble(klass -> delta(cluster, klass, modified)).max().orElse(0);
    }

    /**
     * Computes the fuzzy overlap between two clusters, cluster and klass. In case of modified purity
     * the singleton clusters are ignored.
     *
     * @param cluster  one cluster
     * @param klass    another cluster
     * @param modified whether to use a modified purity
     * @return cluster overlap measure
     * @see <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>
     */
    public double delta(Map<V, Double> cluster, Map<V, Double> klass, boolean modified) {
        if (modified && !(cluster.size() > 1)) return 0;

        final Map<V, Double> intersection = new HashMap<>(cluster);

        intersection.keySet().retainAll(klass.keySet());

        if (intersection.isEmpty()) return 0;

        if (!normalized) return intersection.size();

        return intersection.values().stream().mapToDouble(a -> a).sum();
    }
}
