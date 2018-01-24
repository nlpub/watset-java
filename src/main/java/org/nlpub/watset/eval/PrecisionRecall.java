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

public class PrecisionRecall {
    private final double precision;
    private final double recall;

    public PrecisionRecall(double precision, double recall) {
        this.precision = precision;
        this.recall = recall;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1Score() {
        return getFScore(1);
    }

    public double getFScore(double beta) {
        double beta2 = Math.pow(beta, 2);
        double denominator = beta2 * precision + recall;

        if (denominator == 0d) return 0;

        return (1 + beta2) * precision * recall / denominator;
    }
}
