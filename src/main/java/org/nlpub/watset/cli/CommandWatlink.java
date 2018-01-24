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

package org.nlpub.watset.cli;

import com.beust.jcommander.Parameter;
import org.nlpub.watset.vsm.ContextCosineSimilarity;
import org.nlpub.watset.wsi.Watlink;

class CommandWatlink implements Runnable {
    private final Application application;

    @Parameter(required = true, names = "-k")
    private int k;

    public CommandWatlink(Application application) {
        this.application = application;
    }

    @Override
    public void run() {
        final Watlink<String> watlink = new Watlink<>(null, new ContextCosineSimilarity<>(), k);
        throw new UnsupportedOperationException("Not Implemented Yet");
    }
}
