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

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.util.Sense;

import java.util.Map;

/**
 * A Watset clustering.
 *
 * @param <V> the type of nodes in the graph
 */
public interface WatsetClustering<V> extends ClusteringAlgorithm.Clustering<V> {
    /**
     * Get the intermediate node sense graph built during {@link SimplifiedWatset#getClustering()}.
     *
     * @return the sense graph
     */
    Graph<Sense<V>, DefaultWeightedEdge> getSenseGraph();

    /**
     * Get the disambiguated contexts built during {@link SimplifiedWatset#getClustering()}.
     *
     * @return the disambiguated contexts
     */
    Map<Sense<V>, Map<Sense<V>, Number>> getContexts();
}
