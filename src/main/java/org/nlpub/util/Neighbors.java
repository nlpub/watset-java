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

package org.nlpub.util;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public interface Neighbors {
    static <V, E> Iterator<V> neighborIterator(Graph<V, E> graph, V node) {
        return graph.edgesOf(node).stream().
                map(e -> Graphs.getOppositeVertex(graph, e, node)).
                iterator();
    }

    static <V, E> Set<V> neighborSetOf(Graph<V, E> graph, V node) {
        return graph.edgesOf(node).stream().
                map(e -> Graphs.getOppositeVertex(graph, e, node)).
                collect(Collectors.toSet());
    }
}
