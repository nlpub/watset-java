package org.nlpub.watset.util;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SamplingTest {
    static final Integer[] EMPTY_ARRAY = {};
    static final List<Integer> EMPTY_LIST = Collections.emptyList();

    static final Integer[] ONE_ARRAY = {1};
    static final List<Integer> ONE_LIST = Collections.singletonList(1);

    static final Random random = new Random();

    @Test
    public void testSample0() {
        assertEquals(EMPTY_LIST, Sampling.sample(EMPTY_ARRAY, random));
    }

    @Test
    public void testSample1() {
        assertEquals(ONE_LIST, Sampling.sample(ONE_ARRAY, random));
    }
}
