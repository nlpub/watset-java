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

import org.jgrapht.alg.util.Pair;

import java.util.Locale;

import static java.util.Objects.requireNonNull;

/**
 * A simple integer sense identifier.
 *
 * @param <V> object class.
 */
public class IndexedSense<V> extends Pair<V, Integer> implements Sense<V> {

    /**
     * Creates a sense of an object.
     *
     * @param object an object.
     * @param sense  a sense identifier.
     */
    public IndexedSense(V object, Integer sense) {
        super(object, requireNonNull(sense));
    }

    @Override
    public V get() {
        return first;
    }

    /**
     * Gets a sense identifier.
     *
     * @return a sense identifier.
     */
    public Integer getSense() {
        return second;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s#%d", first, second);
    }
}
