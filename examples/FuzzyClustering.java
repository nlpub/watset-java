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
import org.nlpub.watset.graph.*;
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

        // MaxMax
        var maxmax = new MaxMax.Builder<String, DefaultWeightedEdge>().build(graph);
        var maxmaxClustering = maxmax.getClustering();

        System.out.print("MaxMax Digraph: ");
        System.out.println(maxmaxClustering.getDigraph());

        System.out.print("MaxMax Clusters: ");
        System.out.println(maxmaxClustering.getClusters());

        // Watset[MCL, CW]
        var local = new MarkovClustering.Builder<String, DefaultWeightedEdge>().provider();
        var global = new ChineseWhispers.Builder<Sense<String>, DefaultWeightedEdge>().provider();

        var watset = new SimplifiedWatset.Builder<String, DefaultWeightedEdge>().
                setLocal(local).
                setGlobal(global).
                build(graph);

        var watsetClustering = watset.getClustering();

        System.out.print("Watset Sense Graph: ");
        System.out.println(watsetClustering.getSenseGraph());

        System.out.print("Watset Clusters: ");
        System.out.println(watsetClustering.getClusters());
    }
}
