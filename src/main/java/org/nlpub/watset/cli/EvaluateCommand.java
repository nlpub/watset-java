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

import com.beust.jcommander.Parameter;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.nlpub.watset.util.ILEFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

abstract class EvaluateCommand extends Command {
    /**
     * The gold file.
     */
    @Parameter(required = true, names = {"-g", "--gold"}, description = "Gold file")
    public Path gold;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public EvaluateCommand(MainParameters parameters) {
        super(parameters);
    }

    protected ClusteringAlgorithm.Clustering<String> getClusters() {
        try (final var stream = newInputStream()) {
            return ILEFormat.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected ClusteringAlgorithm.Clustering<String> getClasses() {
        try (final var stream = newInputStream(gold)) {
            return ILEFormat.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
