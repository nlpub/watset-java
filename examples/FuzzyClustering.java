// java -cp watset.jar FuzzyClustering.java

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

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.nlpub.watset.graph.*;
import org.nlpub.watset.util.Sense;

import java.util.function.Function;

public class FuzzyClustering {
    public static void main(String[] args) {
        Graph<String, DefaultWeightedEdge> graph = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
                addVertices("a", "b", "c", "d", "e").
                addEdge("a", "b").
                addEdge("a", "c").
                addEdge("a", "c").
                addEdge("d", "e").
                build();

        System.out.print("Graph: ");
        System.out.println(graph);

        // MaxMax Example
        MaxMax<String, DefaultWeightedEdge> maxmax = new MaxMax<>(graph);
        maxmax.fit();

        System.out.print("MaxMax Digraph: ");
        System.out.println(maxmax.getDigraph());

        System.out.print("MaxMax Clusters: ");
        System.out.println(maxmax.getClusters());

        // Watset Example
        Function<Graph<String, DefaultWeightedEdge>, Clustering<String>> local = ChineseWhispers.provider(NodeWeighting.top());
        Function<Graph<Sense<String>, DefaultWeightedEdge>, Clustering<Sense<String>>> global = ChineseWhispers.provider(NodeWeighting.top());

        SimplifiedWatset<String, DefaultWeightedEdge> watset = new SimplifiedWatset<>(graph, local, global);
        watset.fit();

        System.out.print("Watset Sense Graph: ");
        System.out.println(watset.getSenseGraph());

        System.out.print("Watset Clusters: ");
        System.out.println(watset.getClusters());
    }
}
