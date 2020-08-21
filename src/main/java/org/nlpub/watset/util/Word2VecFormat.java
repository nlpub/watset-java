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

package org.nlpub.watset.util;

import org.nlpub.watset.graph.NodeEmbedding;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilities for handling the Word2Vec text format for node embeddings.
 *
 * @see <a href="https://papers.nips.cc/paper/5021-distributed-representations-of-words-and-phrases-and-their-compositionality.pdf">Mikolov (NIPS 2013)</a>
 * @see <a href="https://radimrehurek.com/gensim/models/keyedvectors.html">gensim: models.keyedvectors &ndash; Store and query word vectors</a>
 * @see <a href="https://code.google.com/archive/p/word2vec/">word2vec: Tool for computing continuous distributed representations of words</a>
 * @see NodeEmbedding
 */
public final class Word2VecFormat {
    /**
     * A pattern that matches a space.
     */
    public final static Pattern SPACES = Pattern.compile(" ");

    /**
     * Space character handling strategy.
     */
    public enum SpaceStrategy {
        /**
         * Do nothing when space occurs.
         */
        @SuppressWarnings("unused") IGNORE,

        /**
         * Throw a {@link IllegalStateException} when space occurs.
         */
        FAIL,

        /**
         * Replace spaces with underscores ({@code _}).
         */
        REPLACE
    }

    private Word2VecFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Write the file in the Word2Vec format representing the node embeddings.
     *
     * @param writer        the writer
     * @param embeddings    the embeddings
     * @param spaceStrategy the space handling strategy
     * @param <V>           the type of nodes in the graph
     * @throws IOException if an I/O error occurs
     */
    public static <V> void write(BufferedWriter writer, List<NodeEmbedding<V>> embeddings, SpaceStrategy spaceStrategy) throws IOException {
        if (embeddings.isEmpty()) {
            throw new IllegalArgumentException("embeddings should not be empty");
        }

        final var df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ROOT));
        df.setMaximumFractionDigits(10);

        final int k = embeddings.get(0).getPoint().length;

        writer.write(Integer.toString(embeddings.size()));
        writer.write(' ');
        writer.write(Integer.toString(k));
        writer.write('\n');

        for (final var node : embeddings) {
            var label = node.get().toString();

            if (label.contains(" ")) {
                switch (spaceStrategy) {
                    case FAIL:
                        throw new IllegalStateException("node has spaces: " + label);
                    case REPLACE:
                        label = SPACES.matcher(label).replaceAll("_");
                        break;
                    default:
                        break;
                }
            }

            writer.write(label);

            for (final double v : node.getPoint()) {
                writer.write(' ');
                writer.write(df.format(v));
            }

            writer.write('\n');
        }
    }
}
