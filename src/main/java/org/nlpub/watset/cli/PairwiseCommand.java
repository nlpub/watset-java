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
import org.nlpub.watset.eval.Pairwise;
import org.nlpub.watset.util.ILEFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.nlpub.watset.eval.Pairwise.transform;

/**
 * A command that performs pairwise cluster evaluation.
 */
@Parameters(commandDescription = "Pairwise Cluster Evaluation")
class PairwiseCommand extends Command {
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
    public PairwiseCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        final var clusters = getClusters().getClusters();
        final var classes = getClasses().getClusters();

        final var pairwise = new Pairwise<String>();
        final var clusterPairs = transform(requireNonNull(clusters));
        final var classPairs = transform(requireNonNull(classes));

        final var results = pairwise.evaluate(clusterPairs, classPairs);

        try (final var writer = newOutputWriter()) {
            writer.write(String.format(Locale.ROOT, "Clusters: %d", clusters.size()));
            writer.write(System.lineSeparator());
            writer.write(String.format(Locale.ROOT, "Cluster Pairs: %d", clusterPairs.size()));
            writer.write(System.lineSeparator());
            writer.write(String.format(Locale.ROOT, "Classes: %d", classes.size()));
            writer.write(System.lineSeparator());
            writer.write(String.format(Locale.ROOT, "Class Pairs: %d", classPairs.size()));
            writer.write(System.lineSeparator());

            writer.write(String.format(Locale.ROOT, "Precision: %f", results.getPrecision()));
            writer.write(System.lineSeparator());
            writer.write(String.format(Locale.ROOT, "Recall: %f", results.getRecall()));
            writer.write(System.lineSeparator());

            writer.write(String.format(Locale.ROOT, "F1: %f", results.getF1Score()));
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ClusteringAlgorithm.Clustering<String> getClusters() {
        try (final var stream = newInputStream()) {
            return ILEFormat.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ClusteringAlgorithm.Clustering<String> getClasses() {
        try (final var stream = newInputStream(gold)) {
            return ILEFormat.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
