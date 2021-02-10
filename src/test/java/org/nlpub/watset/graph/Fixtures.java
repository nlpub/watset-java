/*
 * Copyright 2021 Dmitry Ustalov
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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.Set;

public final class Fixtures {
    private Fixtures() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public final static Graph<String, DefaultWeightedEdge> TWO_COMPONENTS = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e").
            addEdge("a", "b").
            addEdge("a", "c").
            addEdge("a", "c").
            addEdge("d", "e").
            build();

    public final static Graph<String, DefaultWeightedEdge> BIPARTITE = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("0", "1", "2", "3", "4", "5", "6", "7").
            addEdge("0", "1").
            addEdge("0", "2").
            addEdge("0", "3").
            addEdge("1", "2").
            addEdge("1", "3").
            addEdge("2", "3").
            addEdge("2", "6").
            addEdge("4", "5").
            addEdge("4", "6").
            addEdge("4", "7").
            addEdge("5", "6").
            addEdge("5", "7").
            addEdge("6", "7").
            build();

    /**
     * Example from https://www.cs.ucsb.edu/~xyan/classes/CS595D-2009winter/MCL_Presentation2.pdf.
     */
    public final static Graph<String, DefaultWeightedEdge> MCL_GRAPH = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("1", "2", "3", "4").
            addEdge("1", "2").
            addEdge("1", "3").
            addEdge("1", "4").
            addEdge("2", "4").
            build();

    public final static Graph<String, DefaultWeightedEdge> MAXMAX_GRAPH = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("r", "s", "u", "v", "t", "w", "x").
            addEdge("r", "s", 3).
            addEdge("r", "v", 1).
            addEdge("r", "t", 2).
            addEdge("r", "x", 2).
            addEdge("r", "w", 2).
            addEdge("r", "u", 1).
            addEdge("x", "t", 1).
            addEdge("x", "w", 4).
            addEdge("w", "t", 2).
            addEdge("w", "v", 1).
            addEdge("w", "s", 2).
            addEdge("v", "s", 2).
            addEdge("v", "t", 1).
            addEdge("s", "t", 1).
            addEdge("s", "u", 2).
            addEdge("u", "v", 1).
            build();

    public final static List<Set<String>> MAXMAX_CLUSTERS = List.of(Set.of("r", "s", "t", "u", "v"), Set.of("w", "t", "x"));

    public final static Graph<String, DefaultWeightedEdge> FUZZY_GRAPH = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e").
            addEdge("a", "b", 3).
            addEdge("b", "c", 1).
            addEdge("c", "a", 1).
            addEdge("a", "d", 2).
            addEdge("c", "d", 1).
            addEdge("b", "e", 2).
            addEdge("c", "e", 1).
            addEdge("d", "e", 3).
            build();

    public final static List<Set<String>> FUZZY_CLUSTERS = List.of(Set.of("a", "b", "c"), Set.of("c", "d", "e"));

    public final static Graph<String, DefaultWeightedEdge> WORD_GRAPH = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
            addVertices("a", "b", "c", "d", "e", "f", "g", "h").
            addEdge("a", "b", 10).
            addEdge("a", "c", .5).
            addEdge("a", "d").
            addEdge("a", "e", .4).
            addEdge("a", "f").
            addEdge("a", "g").
            addEdge("b", "c", 3).
            addEdge("b", "d").
            addEdge("c", "d").
            addEdge("e", "f", .25).
            build();
}
