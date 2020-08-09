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

package org.nlpub.watset.cli;

import com.beust.jcommander.converters.BaseConverter;

import java.util.Random;

/**
 * Converter of string options to pre-initialized {@link Random}.
 */
@SuppressWarnings("unused")
class RandomConverter extends BaseConverter<Random> {
    /**
     * Create a new instance of {@code RandomConverter}.
     *
     * @param optionName the option name
     */
    RandomConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Random convert(String value) {
        return new Random(Long.parseLong(value));
    }
}
