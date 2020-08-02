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

package org.nlpub.watset.util;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Utilities for extracting neighborhood graphs and iterating over them.
 */
public final class Neighbors {
    private Neighbors() {
        throw new AssertionError();
    }

    /**
     * Extract the neighborhood graph for the given node.
     *
     * @param graph the graph
     * @param node  the target node
     * @param <V>   the type of nodes in the graph
     * @param <E>   the type of edges in the graph
     * @return a neighborhood of {@code node}
     */
    public static <V, E> Graph<V, E> graph(Graph<V, E> graph, V node) {
        final var neighbors = Graphs.neighborSetOf(graph, node);

        final var subgraph = new AsSubgraph<>(graph, neighbors);

        final var builder = SimpleWeightedGraph.
                <V, E>createBuilder(graph.getEdgeSupplier()).
                addGraph(subgraph);

        return builder.buildAsUnmodifiable();
    }
}
