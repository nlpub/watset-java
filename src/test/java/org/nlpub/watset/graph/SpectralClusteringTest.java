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

package org.nlpub.watset.graph;

import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpectralClusteringTest {
    final KMeansPlusPlusClusterer<NodeEmbedding<Integer>> KMEANS = new KMeansPlusPlusClusterer<>(2, -1, new EuclideanDistance(), new JDKRandomGenerator(1337));
    final DBSCANClusterer<NodeEmbedding<Integer>> DBSCAN = new DBSCANClusterer<>(1, 0);
    final SpectralClustering.Builder<Integer, DefaultWeightedEdge> BUILDER = SpectralClustering.<Integer, DefaultWeightedEdge>builder().setK(2);

    @Test
    public void testKMeans() {
        final var spectral = BUILDER.setClusterer(KMEANS).apply(MarkovClusteringTest.TWOCLUSTERS);
        final var clustering = spectral.getClustering();
        assertEquals(2, clustering.getNumberClusters());
    }

    @Test
    public void testDBSCAN() {
        final var spectral = BUILDER.setClusterer(DBSCAN).apply(MarkovClusteringTest.TWOCLUSTERS);
        final var clustering = spectral.getClustering();
        assertEquals(4, clustering.getNumberClusters());
    }
}
