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

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ComponentsClusteringTest {
    private final ComponentsClustering<String, ?> components = new ComponentsClustering<>(ChineseWhispersTest.DISJOINT);

    @Before
    public void setup() {
        components.run();
    }

    @Test
    public void testClustering() {
        final Collection<Collection<String>> clusters = components.getClusters();
        assertEquals(2, clusters.size());
    }
}
