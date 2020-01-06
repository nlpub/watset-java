package org.nlpub.watset.eval;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;
import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.ChineseWhispersTest;
import org.nlpub.watset.graph.NodeWeighting;

import static org.junit.Assert.assertEquals;

public class MeasurerTest {
    static final Measurer<String, DefaultWeightedEdge> MEASURER = new Measurer<>(ChineseWhispers.provider(NodeWeighting.top()), ChineseWhispersTest.DISJOINT);

    @Before
    public void setUp() {
        MEASURER.run();
    }

    @Test
    public void testGetGraph() {
        assertEquals(ChineseWhispersTest.DISJOINT, MEASURER.getGraph());
    }

    @Test
    public void testGetDurations() {
        assertEquals(Measurer.REPETITIONS, MEASURER.getDurations().size());
    }

    @Test
    public void testGetClusters() {
        assertEquals(Measurer.REPETITIONS, MEASURER.getClusters().size());
    }
}
