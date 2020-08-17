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
 * @see <a href="https://papers.nips.cc/paper/5021-distributed-representations-of-words-and-phrases-and-their-compositionality.pdf">Mikolov (NIPS 2013)</a>
 * @see <a href="https://radimrehurek.com/gensim/models/keyedvectors.html">gensim: models.keyedvectors &ndash; Store and query word vectors</a>
 * @see <a href="https://code.google.com/archive/p/word2vec/">word2vec: Tool for computing continuous distributed representations of words</a>
 * @see NodeEmbedding
 */
public final class Word2VecFormat {
    public final static Pattern SPACES = Pattern.compile(" ");

    public enum SpaceStrategy {
        @SuppressWarnings("unused") IGNORE,
        FAIL,
        REPLACE
    }

    private Word2VecFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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
            var label = node.getNode().toString();

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