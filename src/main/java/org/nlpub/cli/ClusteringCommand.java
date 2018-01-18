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

package org.nlpub.cli;

import org.nlpub.graph.Clustering;

import java.io.IOException;

import static org.nlpub.cli.Application.write;

public abstract class ClusteringCommand implements Runnable {
    final Application application;

    public ClusteringCommand(Application application) {
        this.application = application;
    }

    @Override
    public void run() {
        final Clustering<String> clustering = getClustering();
        clustering.run();

        try {
            write(application.output, clustering);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Clustering<String> getClustering();
}
