package org.nlpub.watset.graph;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import java.util.function.Supplier;

/**
 * Coordinates of the graph node.
 *
 * @param <V> the type of nodes in the graph
 */
public class NodeEmbedding<V> extends DoublePoint implements Supplier<V> {
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
