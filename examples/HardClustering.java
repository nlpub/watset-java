// java -cp watset.jar HardClustering.java

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

public class HardClustering {
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

        // Empty Example
        var empty = new EmptyClustering<String>();
        empty.fit();

        System.out.print("Empty Clusters: ");
        System.out.println(empty.getClusters());

        // Singleton Example
        var singleton = new SingletonClustering<>(graph);
        singleton.fit();

        System.out.print("Singleton Clusters: ");
        System.out.println(singleton.getClusters());

        // Together Example
        var together = new TogetherClustering<>(graph);
        together.fit();

        System.out.print("Together Clusters: ");
        System.out.println(together.getClusters());

        // Components Clustering Example
        var components = new ComponentsClustering<>(graph);
        components.fit();

        System.out.print("Components Clusters: ");
        System.out.println(components.getClusters());

        // Chinese Whispers Example
        var cw = new ChineseWhispers<>(graph, NodeWeighting.top());
        cw.fit();

        System.out.print("Chinese Whispers Clusters: ");
        System.out.println(cw.getClusters());

        // Markov Clustering Example
        var mcl = new MarkovClustering<>(graph, 2, 2);
        mcl.fit();

        System.out.print("Markov Clustering Clusters: ");
        System.out.println(mcl.getClusters());
    }
}