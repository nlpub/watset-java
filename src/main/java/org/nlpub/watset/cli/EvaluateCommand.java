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
import java.util.stream.Stream;

/**
 * A command that performs cluster evaluation.
 */
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
    protected EvaluateCommand(MainParameters parameters) {
        super(parameters);
    }

    /**
     * Read, parse, and return the input clusters stored in {@link MainParameters#input}.
     *
     * @return clusters
     * @see ILEFormat#parse(Stream)
     */
    protected ClusteringAlgorithm.Clustering<String> getClusters() {
        try (final var stream = newInputStream()) {
            return ILEFormat.parse(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read, parse, and return the gold clusters stored in {@link EvaluateCommand#gold}.
     *
     * @return gold clusters (aka classes)
     * @see ILEFormat#parse(Stream)
     */
    protected ClusteringAlgorithm.Clustering<String> getClasses() {
        try (final var stream = newInputStream(gold)) {
            return ILEFormat.parse(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
