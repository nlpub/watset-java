/*
 * Copyright 2020 Dmitry Ustalov
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

import net.razorvine.pickle.PickleException;
import net.razorvine.pickle.Unpickler;
import net.razorvine.pickle.objects.ClassDict;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Utilities for handling pickled NetworkX graphs.
 *
 * @see <a href="https://networkx.github.io/">NetworkX</a>
 * @see <a href="https://docs.python.org/3/library/pickle.html">Pickle</a>
 */
@SuppressWarnings("unused")
public final class NetworkXFormat {
    private NetworkXFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Unpickle the NetworkX graph from the input stream.
     *
     * @param stream the input stream with pickled data
     * @return an unpickled NetworkX graph
     * @throws IOException if an I/O error occurs
     */
    public static ClassDict parse(InputStream stream) throws IOException {
        return (ClassDict) (new Unpickler().load(requireNonNull(stream)));
    }

    /**
     * Reconstruct a {@link Graph} object from the unpickled NetworkX graph.
     * <p>
     * Please be careful with the type of the nodes.
     *
     * @param nx  the unpickled NetworkX graph
     * @param <V> the type of nodes in the graph
     * @return a graph represented in the unpickled graph
     */
    public static <V> Graph<V, DefaultWeightedEdge> load(ClassDict nx) {
        requireNonNull(nx);

        if (!nx.get("__class__").equals("networkx.classes.graph.Graph")) {
            throw new PickleException("graph is not networkx.classes.graph.Graph");
        }

        final var builder = SimpleWeightedGraph.<V, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class);

        @SuppressWarnings("unchecked") final var nodes = (Map<V, Object>) nx.get("_node");

        for (var node : nodes.keySet()) {
            builder.addVertex(node);
        }

        @SuppressWarnings("unchecked") final var edges = (Map<V, Map<V, Object>>) nx.get("_adj");

        for (var source : edges.entrySet()) {
            for (var target : source.getValue().keySet()) {
                builder.addEdge(source.getKey(), target);
            }
        }

        return builder.build();
    }
}
