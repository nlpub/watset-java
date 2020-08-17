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

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
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

        // Empty Clustering
        var empty = EmptyClustering.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Empty: ");
        System.out.println(empty.getClustering());

        // Singleton Clustering
        var singleton = SingletonClustering.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Singleton: ");
        System.out.println(singleton.getClustering());

        // Together Clustering
        var together = TogetherClustering.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Together: ");
        System.out.println(together.getClustering());

        // Connected Components
        var components = ComponentsClustering.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Components: ");
        System.out.println(components.getClustering());

        // k Spanning Tree Clustering
        var kst = new KSpanningTreeClustering<>(graph, 2);
        System.out.print("k Spanning Tree (k=2): ");
        System.out.println(kst.getClustering());

        // Spectral Clustering with k-means++
        var kmeans = new KMeansPlusPlusClusterer<NodeEmbedding<String>>(2);
        var spectral = new SpectralClustering<>(graph, kmeans, 2);
        System.out.print("Spectral Clustering (k=2): ");
        System.out.println(spectral.getClustering());

        // Chinese Whispers
        var cw = ChineseWhispers.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Chinese Whispers: ");
        System.out.println(cw.getClustering());

        // Markov Clustering
        var mcl = MarkovClustering.<String, DefaultWeightedEdge>builder().apply(graph);
        System.out.print("Markov Clustering: ");
        System.out.println(mcl.getClustering());
    }
}
