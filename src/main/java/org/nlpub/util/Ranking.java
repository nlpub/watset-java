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

package org.nlpub.util;

import java.util.Map;
import java.util.stream.Collectors;

public interface Ranking {
    static <V> Map<V, Number> getTopK(Map<V, Number> vector, long k) {
        return vector.entrySet().stream().
                sorted((e1, e2) -> Double.compare(e2.getValue().doubleValue(), e1.getValue().doubleValue())).limit(k).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
