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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpectralClusteringTest {
    final KMeansPlusPlusClusterer<SpectralClustering.NodeEmbedding<Integer>> kmeans = new KMeansPlusPlusClusterer<>(2, -1, new EuclideanDistance(), new JDKRandomGenerator(1337));
    final DBSCANClusterer<SpectralClustering.NodeEmbedding<Integer>> dbscan = new DBSCANClusterer<>(1, 0);

    @Test
    public void testKMeans() {
        final var spectral = new SpectralClustering<>(MarkovClusteringTest.TWOCLUSTERS, kmeans, 2);
        final var clustering = spectral.getClustering();
        assertEquals(2, clustering.getNumberClusters());
    }

    @Test
    public void testDBSCAN() {
        final var spectral = new SpectralClustering<>(MarkovClusteringTest.TWOCLUSTERS, dbscan, 2);
        final var clustering = spectral.getClustering();
        assertEquals(4, clustering.getNumberClusters());
    }
}
