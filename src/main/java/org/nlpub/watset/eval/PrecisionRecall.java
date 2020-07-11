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

package org.nlpub.watset.eval;

/**
 * A wrapper for precision and recall values that computes F-score.
 *
 * @see <a href="https://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-unranked-retrieval-sets-1.html">Evaluation of unranked retrieval sets</a>
 */
public class PrecisionRecall {
    private final double precision;
    private final double recall;

    /**
     * Wrap the precision and recall values.
     *
     * @param precision precision
     * @param recall    recall
     */
    public PrecisionRecall(double precision, double recall) {
        this.precision = precision;
        this.recall = recall;
    }

    /**
     * Get the value of precision.
     *
     * @return precision
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Get the value of recall.
     *
     * @return recall
     */
    public double getRecall() {
        return recall;
    }

    /**
     * Compute the F<sub>1</sub>-score using precision and recall.
     *
     * @return F<sub>1</sub>-score
     */
    public double getF1Score() {
        return getFScore(1);
    }

    /**
     * Compute the F<sub>&beta;</sub>-score using precision and recall.
     *
     * @param beta beta value
     * @return F<sub>&beta;</sub>-score value
     */
    public double getFScore(double beta) {
        var beta2 = StrictMath.pow(beta, 2);
        var denominator = beta2 * precision + recall;

        if (denominator == 0d) return 0;

        return (1 + beta2) * precision * recall / denominator;
    }
}
