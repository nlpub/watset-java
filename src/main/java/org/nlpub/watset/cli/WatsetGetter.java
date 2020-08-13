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

package org.nlpub.watset.cli;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.ClusteringAlgorithmBuilder;
import org.nlpub.watset.graph.SimplifiedWatset;
import org.nlpub.watset.util.Sense;

import java.util.logging.Logger;

/**
 * Routines for constructing instances of two Watset variations.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
interface WatsetGetter<V, E> {
    /**
     * Remind about deprecation of {@link org.nlpub.watset.graph.Watset}.
     *
     * @param logger     the logger
     * @param simplified Watset is simplified
     */
    default void notifySimplifiedWatset(Logger logger, boolean simplified) {
        if (simplified) {
            logger.warning("Watset is always simplified; the -s flag makes no effect.");
        } else {
            logger.info("Simplified Watset is used instead of the regular Watset.");
        }
    }

    /**
     * Construct an instance of {@link SimplifiedWatset}.
     *
     * @param local  the local clustering algorithm supplier
     * @param global the global clustering algorithm supplier
     * @param graph  the graph
     * @return an instance of Simplified Watset
     */
    default SimplifiedWatset<V, E> getWatset(ClusteringAlgorithmBuilder<V, E, ?> local, ClusteringAlgorithmBuilder<Sense<V>, DefaultWeightedEdge, ?> global, Graph<V, E> graph) {
        return SimplifiedWatset.<V, E>builder().
                setLocal(local).
                setGlobal(global).
                apply(graph);
    }
}
