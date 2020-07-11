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

import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.NodeWeighting;
import org.nlpub.watset.util.NetworkXFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PickleClustering {
    public static void main(String[] args) throws IOException {
        try (var stream = Files.newInputStream(Paths.get("karate_club_graph.pkl"))) {
            var graph = NetworkXFormat.load(NetworkXFormat.parse(stream));

            var cw = new ChineseWhispers<>(graph, NodeWeighting.top());
            cw.fit();

            System.out.print("Chinese Whispers Clusters: ");
            System.out.println(cw.getClusters());
        }
    }
}
