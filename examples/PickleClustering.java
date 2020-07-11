// python3 PickleGraph.py
// java -cp watset.jar PickleClustering.java

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

import net.razorvine.pickle.Unpickler;
import net.razorvine.pickle.objects.ClassDict;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.*;
import org.nlpub.watset.util.NetworkXFormat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PickleClustering {
    public static void main(String[] args) throws IOException {
        try (InputStream stream = Files.newInputStream(Paths.get("karate_club_graph.pkl"))) {
            ClassDict nx = NetworkXFormat.parse(stream);
            Graph<Object, DefaultWeightedEdge> graph = NetworkXFormat.load(nx);

            ChineseWhispers<Object, DefaultWeightedEdge> cw = new ChineseWhispers<>(graph, NodeWeighting.top());
            cw.fit();

            System.out.print("Chinese Whispers Clusters: ");
            System.out.println(cw.getClusters());
        }
    }
}
