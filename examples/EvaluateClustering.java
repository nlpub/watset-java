// java -cp watset.jar EvaluateClustering

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

import org.nlpub.watset.eval.NormalizedModifiedPurity;
import org.nlpub.watset.eval.Pairwise;

import java.util.List;
import java.util.Set;

public class EvaluateClustering {
    public static void main(String[] args) {
        var clusters = List.of(
            Set.of("bank"),
            Set.of("bank", "building"),
            Set.of("riverbank", "streambank", "streamside"),
            Set.of("bank building")
        );

        var classes = List.of(
            Set.of("bank", "riverbank", "streambank", "streamside"),
            Set.of("bank", "building", "bank building")
        );

        var clusterPairs = Pairwise.transform(clusters);
        var classPairs = Pairwise.transform(classes);

        var pairwise = new Pairwise<String>();
        var pairwiseResults = pairwise.evaluate(clusterPairs, classPairs);
        System.out.print("Pairwise ");
        System.out.println(pairwiseResults);

        var clusterCounts = NormalizedModifiedPurity.normalize(NormalizedModifiedPurity.transform(clusters));
        var classCounts = NormalizedModifiedPurity.normalize(NormalizedModifiedPurity.transform(classes));

        var nmPU = new NormalizedModifiedPurity<String>(true, true);
        var niPU = new NormalizedModifiedPurity<String>(true, false);
        var purityResults = NormalizedModifiedPurity.evaluate(nmPU, niPU, clusterCounts, classCounts);
        System.out.print("Normalized Modified Purity ");
        System.out.println(purityResults);
    }
}
