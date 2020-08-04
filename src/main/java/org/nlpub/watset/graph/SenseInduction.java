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

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.nlpub.watset.util.Neighbors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.jgrapht.GraphTests.requireUndirected;

/**
 * A simple graph-based word sense induction approach that clusters node neighborhoods.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.3115/1654758.1654774">Biemann (TextGraphs-1)</a>
 * @see <a href="https://doi.org/10.3115/1067737.1067753">Dorow &amp; Widdows (EACL '03)</a>
 * @see <a href="https://doi.org/10.1162/COLI_a_00354">Ustalov et al. (COLI 45:3)</a>
 */
public class SenseInduction<V, E> {
    /**
     * The graph.
     */
    protected final Graph<V, E> graph;

    /**
     * The local clustering algorithm supplier.
     */
    protected final Function<Graph<V, E>, ClusteringAlgorithm<V>> local;

    /**
     * Create an instance of {@code SenseInduction}.
     *
     * @param graph the graph
     * @param local the neighborhood clustering algorithm supplier
     */
    public SenseInduction(Graph<V, E> graph, Function<Graph<V, E>, ClusteringAlgorithm<V>> local) {
        this.graph = requireUndirected(graph);
        this.local = requireNonNull(local);
    }

    /**
     * Get the induced sense clusters.
     *
     * @param target the target node
     * @return a map of senses to their contexts
     */
    public ClusteringAlgorithm.Clustering<V> clustering(V target) {
        final var ego = Neighbors.graph(graph, requireNonNull(target));
        final var clustering = local.apply(ego);
        return clustering.getClustering();
    }

    /**
     * Get the induced senses and their non-disambiguated contexts.
     *
     * @param target the target node
     * @return maps of senses to their contexts
     */
    public List<Map<V, Number>> contexts(V target) {
        final var clustering = clustering(target);

        final var senses = new ArrayList<Map<V, Number>>(clustering.getNumberClusters());

        for (final var cluster : clustering.getClusters()) {
            final var context = new HashMap<V, Number>(cluster.size());

            for (final var neighbor : cluster) {
                context.put(neighbor, graph.getEdgeWeight(graph.getEdge(target, neighbor)));
            }

            senses.add(context);
        }

        return senses;
    }
}
