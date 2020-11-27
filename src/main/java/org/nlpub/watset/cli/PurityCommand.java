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
import com.beust.jcommander.Parameters;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.nlpub.watset.eval.NormalizedModifiedPurity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A command that evaluates clusters with purity.
 */
@Parameters(commandDescription = "Cluster Evaluation with Purity")
class PurityCommand extends EvaluateCommand {
    @Parameter(names = {"-n", "--normalized"}, description = "Use normalized purity")
    public boolean normalized;

    @Parameter(names = {"-m", "--modified"}, description = "Use modified purity")
    public boolean modified;

    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public PurityCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        final var clusters = transform(getClusters());
        final var classes = transform(getClasses());

        final var precision = new NormalizedModifiedPurity<String>(normalized, modified);
        final var recall = new NormalizedModifiedPurity<String>(normalized, false); // this is intentional

        final var results = NormalizedModifiedPurity.evaluate(precision, recall, clusters, classes);

        try (final var writer = newOutputWriter()) {
            writer.write(String.format(Locale.ROOT, "Clusters: %d", clusters.size()));
            writer.write(System.lineSeparator());
            writer.write(String.format(Locale.ROOT, "Classes: %d", classes.size()));
            writer.write(System.lineSeparator());

            if (normalized && modified) {
                writer.write(String.format(Locale.ROOT, "nmPU: %f", results.getPrecision()));
            } else if (normalized) {
                writer.write(String.format(Locale.ROOT, "nPU: %f", results.getPrecision()));
            } else if (modified) {
                writer.write(String.format(Locale.ROOT, "mPU: %f", results.getPrecision()));
            } else {
                writer.write(String.format(Locale.ROOT, "PU: %f", results.getPrecision()));
            }
            writer.write(System.lineSeparator());

            if (normalized) {
                writer.write(String.format(Locale.ROOT, "niPU: %f", results.getRecall()));
            } else {
                writer.write(String.format(Locale.ROOT, "iPU: %f", results.getRecall()));
            }
            writer.write(System.lineSeparator());

            writer.write(String.format(Locale.ROOT, "F1: %f", results.getF1Score()));
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Map<String, Double>> transform(ClusteringAlgorithm.Clustering<String> clustering) {
        final var transformed = NormalizedModifiedPurity.transform(clustering.getClusters());
        return normalized ? NormalizedModifiedPurity.normalize(transformed) : transformed;
    }
}
