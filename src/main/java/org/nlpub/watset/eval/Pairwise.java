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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Pairwise precision, recall, and F-score for cluster evaluation.
 *
 * @param <V> the type of cluster elements
 * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">Evaluation of clustering</a>
 * @see <a href="https://aclweb.org/anthology/S10-1011">Manandhar et al. (SemEval 2010)</a>
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 */
public class Pairwise<V> {
    /**
     * Transform a collection of clusters to a collection of pairs
     * generated using 2-combinations of the cluster elements.
     *
     * @param clusters the collection of clusters
     * @param <V>      the type of cluster elements
     * @return a collection of pairs
     */
    public static <V> Set<Pair<V, V>> transform(Collection<? extends Collection<V>> clusters) {
        return clusters.parallelStream().flatMap(Pairwise::combination).collect(Collectors.toSet());
    }

    /**
     * Return a stream of pairs generated as 2-combinations of the cluster elements.
     *
     * @param cluster the cluster
     * @param <V>     the type of cluster elements
     * @return a stream of 2-combinations
     */
    public static <V> Stream<Pair<V, V>> combination(Collection<V> cluster) {
        return cluster.stream().
                flatMap(first -> cluster.stream().map(second -> pairOf(first, second))).
                filter(pair -> !pair.getFirst().equals(pair.getSecond()));
    }

    /**
     * Create a pair of elements ordered by hashCode.
     *
     * @param first  the first object
     * @param second the second object
     * @param <V>    the type of objects
     * @return a pair
     */
    public static <V> Pair<V, V> pairOf(V first, V second) {
        return (first.hashCode() <= second.hashCode()) ? Pair.of(first, second) : Pair.of(second, first);
    }

    /**
     * Compute a pairwise precision, recall, and F-score.
     *
     * @param clusterPairs the cluster pairs to evaluate
     * @param classPairs   the gold standard pairs to evaluate
     * @return precision and recalled wrapped in an instance of {@link PrecisionRecall}
     */
    public PrecisionRecall evaluate(Set<Pair<V, V>> clusterPairs, Set<Pair<V, V>> classPairs) {
        final var union = new HashSet<>(clusterPairs);
        union.addAll(classPairs);

        final var preds = new boolean[union.size()];
        final var trues = new boolean[union.size()];

        var i = 0;

        for (final var pair : union) {
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

    /**
     * Transform the clusters to pairs and compute a pairwise precision, recall, and F-score.
     *
     * @param clusters the collection of the clusters to evaluate
     * @param classes  the collection of the gold standard clusters
     * @return precision and recalled wrapped in an instance of {@link PrecisionRecall}
     */
    public PrecisionRecall evaluate(Collection<? extends Collection<V>> clusters, Collection<? extends Collection<V>> classes) {
        final var clusterPairs = transform(requireNonNull(clusters));
        final var classPairs = transform(requireNonNull(classes));
        return evaluate(clusterPairs, classPairs);
    }
}
