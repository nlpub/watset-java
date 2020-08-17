package org.nlpub.watset.graph;

import org.apache.commons.math3.ml.clustering.DoublePoint;

public class NodeEmbedding<V> extends DoublePoint {
    private final V node;

    public NodeEmbedding(V node, double[] point) {
        super(point);
        this.node = node;
    }

    public V getNode() {
        return node;
    }
}
