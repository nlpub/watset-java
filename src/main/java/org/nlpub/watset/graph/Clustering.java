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

package org.nlpub.watset.graph;

import java.util.Collection;

/**
 * An instance of Clustering returns clusters after running the underlying algorithm.
 *
 * @param <V> node class.
 */
public interface Clustering<V> {
    /**
     * Runs the algorithm to induce the parameters of the clusters.
     *
     * @return itself.
     */
    Clustering<V> fit();

    /**
     * Return a collection of clusters, each cluster is a collection of objects.
     * Usually this method is called after the fit method.
     *
     * @return clusters.
     */
    Collection<Collection<V>> getClusters();
}
