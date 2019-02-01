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

import org.jgrapht.alg.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * An implementation of a pairwise precision, recall, and F-score.
 *
 * @param <V> a cluster element type.
 * @see <a href="https://aclweb.org/anthology/S10-1011">Manandhar et al. (SemEval 2010)</a>
 * @see <a href="https://doi.org/10.18653/v1/P17-1145">Ustalov et al. (ACL 2017)</a>
 */
public class Pairwise<V> {
    /**
     * Transforms a collection of clusters into a collection of pairs
     * generated using 2-combinations of the cluster elements.
     *
     * @param clusters a collection of clusters.
     * @param <V>      an element class.
     * @return
     */
    public static <V> Set<Pair<V, V>> transform(Collection<Collection<V>> clusters) {
        return clusters.parallelStream().flatMap(Pairwise::combination).collect(toSet());
    }

    /**
     * Returns a stream of pairs generated as 2-combinations of the cluster elements.
     *
     * @param cluster a cluster.
     * @param <V>     an element class.
     * @return a stream of 2-combinations.
     */
    public static <V> Stream<Pair<V, V>> combination(Collection<V> cluster) {
        return cluster.stream().
                flatMap(first -> cluster.stream().map(second -> pairOf(first, second))).
                filter(pair -> !pair.getFirst().equals(pair.getSecond()));
    }

    /**
     * Creates a pair of elements ordered by hashCode.
     *
     * @param first  an object.
     * @param second an object.
     * @param <V>    an object type.
     * @return a pair.
     */
    public static <V> Pair<V, V> pairOf(V first, V second) {
        return (first.hashCode() <= second.hashCode()) ? Pair.of(first, second) : Pair.of(second, first);
    }

    /**
     * Computes a pairwise precision, recall, and F-score.
     *
     * @param clusters a collection of clusters.
     * @param classes  a collection of gold standard clusters.
     * @return precision-recall report.
     */
    public PrecisionRecall evaluate(Collection<Collection<V>> clusters, Collection<Collection<V>> classes) {
        final Set<Pair<V, V>> clusterPairs = transform(requireNonNull(clusters));
        final Set<Pair<V, V>> classPairs = transform(requireNonNull(classes));

        final Set<Pair<V, V>> union = new HashSet<>(clusterPairs);
        union.addAll(classPairs);

        final boolean[] preds = new boolean[union.size()];
        final boolean[] trues = new boolean[union.size()];

        int i = 0;

        for (final Pair<V, V> pair : union) {
            preds[i] = clusterPairs.contains(pair);
            trues[i] = classPairs.contains(pair);
            i++;
        }

        int tp = 0, fp = 0, fn = 0;

        for (i = 0; i < union.size(); i++) {
            if ((preds[i]) && (trues[i])) tp++;
            if ((preds[i]) && (!trues[i])) fp++;
            if (!(preds[i]) && (trues[i])) fn++;
        }

        double tp_fp = tp + fp, tp_fn = tp + fn;

        return new PrecisionRecall(tp_fp == 0d ? 0 : tp / tp_fp, tp_fn == 0d ? 0 : tp / tp_fn);
    }
}
