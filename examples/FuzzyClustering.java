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

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.MaxMax;
import org.nlpub.watset.graph.NodeWeighting;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.util.Sense;

public class FuzzyClustering {
    public static void main(String[] args) {
        var graph = SimpleWeightedGraph.<String, DefaultWeightedEdge>createBuilder(DefaultWeightedEdge.class).
                addVertices("a", "b", "c", "d", "e").
                addEdge("a", "b").
                addEdge("a", "c").
                addEdge("a", "c").
                addEdge("d", "e").
                build();

        System.out.print("Graph: ");
        System.out.println(graph);

        // MaxMax Example
        var maxmax = new MaxMax<>(graph);
        maxmax.fit();

        System.out.print("MaxMax Digraph: ");
        System.out.println(maxmax.getDigraph());

        System.out.print("MaxMax Clusters: ");
        System.out.println(maxmax.getClusters());

        // Watset Example
        var local = ChineseWhispers.<String, DefaultWeightedEdge>provider(NodeWeighting.top());
        var global = ChineseWhispers.<Sense<String>, DefaultWeightedEdge>provider(NodeWeighting.top());

        var watset = new SimplifiedWatset<>(graph, local, global);
        watset.fit();

        System.out.print("Watset Sense Graph: ");
        System.out.println(watset.getSenseGraph());

        System.out.print("Watset Clusters: ");
        System.out.println(watset.getClusters());
    }
}
