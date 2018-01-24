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

import org.jgrapht.Graph;
import org.jgrapht.alg.ConnectivityInspector;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class ComponentsClustering<V, E> implements Clustering<V> {
    private final Graph<V, E> graph;
    private Collection<Collection<V>> clusters = null;

    public ComponentsClustering(Graph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return requireNonNull(clusters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        final ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(graph);
        // TODO: Is there a simpler approach?
        clusters = (Collection<Collection<V>>) (Collection<? extends Collection<V>>) inspector.connectedSets();
    }
}
