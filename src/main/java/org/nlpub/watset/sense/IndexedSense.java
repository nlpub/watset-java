/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.watset.sense;

import org.jgrapht.alg.util.Pair;

public class IndexedSense<V> extends Pair<V, Integer> implements Sense<V> {
    public IndexedSense(V v, Integer sense) {
        super(v, sense);
    }

    @Override
    public V get() {
        return super.first;
    }
}
