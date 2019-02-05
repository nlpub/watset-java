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

package org.nlpub.watset.wsi;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;
import org.nlpub.watset.graph.Watset;
import org.nlpub.watset.util.CosineContextSimilarity;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.nlpub.watset.graph.WatsetTest.global;
import static org.nlpub.watset.graph.WatsetTest.local;
import static org.nlpub.watset.wsi.SenseInductionTest.WORDS;

public class WatlinkTest {
    private final static Watset<String, DefaultWeightedEdge> watset = new Watset<>(WORDS, local, global, new CosineContextSimilarity<>());

    private final static Map<String, Collection<String>> hypernyms = new HashMap<String, Collection<String>>() {{
        put("a", Arrays.asList("e", "f"));
        put("b", Arrays.asList("e", "g"));
    }};

    private Watlink<String> watlink;

    @Before
    public void setup() {
        watset.fit();
        watlink = new Watlink<>(watset.getInventory(), new CosineContextSimilarity<>(), 1);
    }

    @Test
    public void testRetrieval() {
        final Map<Sense<String>, Number> context = watlink.retrieve(Arrays.asList("a", "b", "c"), hypernyms);
        final Sense<String> gold = IndexedSense.of("e", 0);
        assertEquals(Collections.singleton(gold), context.keySet());
        assertEquals(2, context.get(gold).intValue());
    }
}
