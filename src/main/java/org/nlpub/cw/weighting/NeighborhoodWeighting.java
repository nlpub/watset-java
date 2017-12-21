/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.cw.weighting;

import org.jgrapht.Graph;
import org.nlpub.util.Neighbors;

import java.util.Iterator;
import java.util.Optional;

import static org.nlpub.util.Maximizer.argmax;

public abstract class NeighborhoodWeighting<V, E> implements NodeSelector<V, E> {
    @Override
    public Optional<V> apply(Graph<V, E> graph, V node) {
        final Iterator<V> neighbors = Neighbors.neighborInterator(graph, node);
        return argmax(neighbors, neighbor -> !(node == neighbor), neighbor -> getScore(graph, node, neighbor));
    }

    protected abstract double getScore(Graph<V, E> graph, V node, V neighbor);
}
