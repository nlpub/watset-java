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

package org.nlpub.eval;

import java.util.Collection;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class NormalizedModifiedPurity<V> {
    private final boolean multi;

    private final Map<Collection<V>, Map<V, Double>> classes;
    private final Map<Collection<V>, Map<V, Double>> clusters;

    public NormalizedModifiedPurity(boolean multi, Collection<Collection<V>> expected, Collection<Collection<V>> actual) {
        this.multi = multi;
        this.classes = initialize(expected);
        this.clusters = initialize(actual);
    }

    public double getNormalizedModifiedPurity() {
        final double correct = clusters.values().stream().
                mapToDouble(cluster -> cluster.size() > 1 ? evaluate(cluster, classes) : 0).
                sum();

        final double total = clusters.values().stream().
                mapToDouble(this::clusterWeight).sum();

        return correct / total;
    }

    public double getNormalizedInversePurity() {
        final double correct = classes.values().stream().
                mapToDouble(klass -> evaluate(klass, clusters)).
                sum();

        final double total = classes.values().stream().
                mapToDouble(this::clusterWeight).
                sum();

        return correct / total;
    }

    private double evaluate(Map<V, Double> cluster, Map<Collection<V>, Map<V, Double>> classes) {
        return classes.keySet().stream().mapToDouble(klass -> cluster.entrySet().stream().
                mapToDouble(element -> {
                    if (!klass.contains(element.getKey())) return 0;
                    if (multi) return element.getValue();
                    return 1;
                }).sum()
        ).max().orElseThrow(() -> new IllegalArgumentException("Do not pass me empty arrays"));
    }

    private double clusterWeight(Map<V, Double> cluster) {
        if (!multi) return cluster.size();
        return cluster.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getFScore() {
        final double denominator = getNormalizedModifiedPurity() + getNormalizedInversePurity();
        if (denominator == 0d) return 0d;
        return 2 * getNormalizedModifiedPurity() * getNormalizedInversePurity() / denominator;
    }

    private Map<Collection<V>, Map<V, Double>> initialize(Collection<Collection<V>> clusters) {
        final Map<Collection<V>, Map<V, Double>> instances = clusters.stream().
                collect(toMap(identity(), cluster ->
                        cluster.stream().collect(groupingBy(identity(), summingDouble(element -> 1)))
                ));

        final Map<V, Integer> counter = clusters.stream().flatMap(Collection::stream).collect(groupingBy(identity(), summingInt(element -> 1)));

        if (multi) {
            for (final Map.Entry<Collection<V>, Map<V, Double>> instance : instances.entrySet()) {
                for (final Map.Entry<V, Double> element : instance.getValue().entrySet()) {
                    instance.getValue().put(element.getKey(), element.getValue() / counter.get(element.getKey()));
                }
            }
        }

        return instances;
    }
}
