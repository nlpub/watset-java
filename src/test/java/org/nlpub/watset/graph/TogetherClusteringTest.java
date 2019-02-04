/*
 * Copyright 2018 Dmitry Ustalov
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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class TogetherClusteringTest {
    private final TogetherClustering<String, ?> together = new TogetherClustering<>(ChineseWhispersTest.DISJOINT);

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = together.getClusters();
        assertEquals(1, clusters.size());
        Assert.assertEquals(ChineseWhispersTest.DISJOINT.vertexSet(), clusters.toArray()[0]);
    }
}
