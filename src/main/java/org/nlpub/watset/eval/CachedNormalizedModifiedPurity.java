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

package org.nlpub.watset.eval;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 * <p>
 * This class creates an instance of {@link ConcurrentHashMap} to cache the scores
 * for the elements of clusters. It is designed under an assumption that the
 * <code>classes</code> <em>do not change</em>. This assumption is valid for computing
 * the precision (nmPU) in case of normalized modified purity as defined by
 * <a href="https://doi.org/10.3115/v1/P14-1097">Kawahara et al. (ACL 2014)</a>.
 * <p>
 * Since the underlying data structure has no size limit, so this class is memory-greedy.
 * Please make sure that you have tuned the JVM heap size when using it.
 */
public class CachedNormalizedModifiedPurity<V> extends NormalizedModifiedPurity<V> {
    protected final Map<Map<V, Double>, Double> cache;

    /**
     * Constructs a cached normalized modified purity calculator.
     */
    public CachedNormalizedModifiedPurity() {
        this(true, true);
    }

    /**
     * Constructs a cached normalized modified purity calculator that allows
     * turning normalized and/or modified options off.
     *
     * @param normalized normalized purity is on.
     * @param modified   modified purity is on.
     */
    public CachedNormalizedModifiedPurity(boolean normalized, boolean modified) {
        super(normalized, modified);
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public double score(Map<V, Double> cluster, Collection<Map<V, Double>> classes) {
        return cache.computeIfAbsent(cluster, c -> super.score(cluster, classes));
    }
}
