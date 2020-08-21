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

import org.apache.commons.math3.ml.clustering.DoublePoint;

import java.util.function.Supplier;

/**
 * Coordinates of the graph node.
 *
 * @param <V> the type of nodes in the graph
 */
public class NodeEmbedding<V> extends DoublePoint implements Supplier<V> {
    /**
     * The node.
     */
    private final V node;

    /**
     * Create an instance of node coordinates.
     *
     * @param node  the node
     * @param point the coordinates
     */
    public NodeEmbedding(V node, double[] point) {
        super(point);
        this.node = node;
    }

    @Override
    public V get() {
        return node;
    }
}
