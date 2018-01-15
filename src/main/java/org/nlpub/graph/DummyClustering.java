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

package org.nlpub.graph;

import org.jgrapht.Graph;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class DummyClustering<V> implements Clustering<V> {
    public static <V, E> Function<Graph<V, E>, Clustering<V>> provider() {
        return graph -> new DummyClustering<>();
    }

    @Override
    public Collection<Collection<V>> getClusters() {
        return Collections.emptySet();
    }

    @Override
    public void run() {
    }
}
