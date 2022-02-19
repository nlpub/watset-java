# Watset

This is an open source implementation of the [Watset] algorithm for [fuzzy graph clustering](https://en.wikipedia.org/wiki/Fuzzy_clustering) (aka soft clustering). This implementation is multi-threaded, extensible, contains several unit tests, and can be easily embedded into any Java-based graph processing pipeline. Also, it offers a convenient command-line tool for running [Watset] over the edge lists in text files.

![Watset](src/main/javadoc/doc-files/Watset.svg)

This package, written in Java, also includes implementations of the [Chinese Whispers] (2006), [Markov Clustering] (2000), and [MaxMax] (2013) graph clustering algorithms.

[![Build Status][github_tests_badge]][github_tests_link] [![Maven Central][maven_badge]][maven_link] [![Maintainability][codeclimate_badge]][codeclimate_link] [![Javadoc][javadoc_badge]][javadoc_link]

[github_tests_badge]: https://github.com/nlpub/watset-java/workflows/Unit%20Tests/badge.svg?branch=master
[github_tests_link]: https://github.com/nlpub/watset-java/actions?query=workflow%3A%22Unit+Tests%22
[maven_badge]: https://img.shields.io/maven-central/v/org.nlpub/watset.svg?label=Maven%20Central
[maven_link]: https://search.maven.org/artifact/org.nlpub/watset
[codeclimate_badge]: https://api.codeclimate.com/v1/badges/2f2a90dd42ae703e9e5d/maintainability
[codeclimate_link]: https://codeclimate.com/github/nlpub/watset-java/maintainability
[javadoc_badge]: https://img.shields.io/badge/javadoc-master-brightgreen
[javadoc_link]: https://nlpub.github.io/watset-java/

## Usage

There are three ways to obtain `watset-java`:

1. download the [recently released version](https://github.com/nlpub/watset-java/releases/latest) (look at the `watset.jar` file in the Assets section);

2. compile the `master` branch from source by cloning the [repository](https://github.com/nlpub/watset-java) and running `mvn package` in the repository root (or `mvn package -Dshade` to build a dependency-less jar file as in the previous option);

3. get it from [Maven Central][maven_link], [GitHub Packages](https://github.com/nlpub/watset-java/packages), or [JitPack](https://jitpack.io/#nlpub/watset-java).

This tool has several kinds of activity titled the *commands*. Such commands as `watset`, `cw`, `mcl`, and `maxmax` invoke different clustering algorithms, while `senses` invokes graph-based word sense induction.

There are two global command-line arguments: `-i` (or `--input`) that specifies the path of input file and `-o` (or `--output`) that specifies the path of the output file. The default values for these parameters are STDIN and STDOUT, correspondingly.

The input for this tool is an undirected weighted graph represented in the ABC format. This format is a tab-separated edge list: <code>word1&#9;word2&#9;weight</code>. The output of the graph clustering algorithm is a tab-separated file containing three columns: the cluster identifier, the cluster size, and the list of elements belonging to the cluster.

```
a	b	1
b	c	0.5
c	d	2
```

The default output format is a tab-separated file, each line of which contains three elements: `id`, `size` and a comma-and-space-separated list of elements.

In fact, `watset-java` is not only a tool, it also features a complete API for graph clustering. This API can easily be embedded into your application or library, see [examples](examples/). The complete Javadoc is available at <https://nlpub.github.io/watset-java/>.

### Dummy Clustering

As the baseline, it is possible to use the following four dummy clustering &ldquo;algorithms&rdquo;:

* `empty`: the empty output regardless of the input;
* `singleton`: each node of the input graph is a different cluster;
* `together`: one cluster that keeps all the elements of the input graph;
* `components`: each cluster is a connected component of the input graph.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv singleton
```

### k Spanning Tree Clustering

The [*k* spanning tree clustering algorithm](https://doi.org/10.2307/2346439) performs graph single-linkage clustering using minimal spanning trees.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv kst -k 2
```

### Spectral Clustering

The [spectral clustering](https://en.wikipedia.org/wiki/Spectral_clustering) algorithm performs clustering of the graph's spectral embedding with the [*k*-means algorithm](https://en.wikipedia.org/wiki/K-means_clustering).

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv spectral -k 2
```

Since this algorithm computes node embeddings, it is possible to output them in the well-known *textual* [Word2Vec format](https://code.google.com/archive/p/word2vec/).

```
$ java -jar watset.jar -i graph.txt -o output.w2v embed -k 2
```

### Chinese Whispers

[Chinese Whispers] (CW) is a hard clustering algorithm that resembles a popular children's game. This tool offers three different variations of this algorithm that can be set using the `-m` (`--mode`) option:

* `top`: the node weighting approach that chooses the label with the highest total edge weight in the neighborhood;
* `lin`: the `top` mode divided by the node degree (also known as `nolog` in some references);
* `log`: the `top` mode divided by the logarithm of the node degree.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv cw -m top
```

### Markov Clustering

[Markov Clustering] (MCL) is a hard clustering algorithm that simulates random walks on the graph. It is possible to specify two options of this algorithm:

* the number of expansion operations `-e` (the default value is 2);
* the power coefficient `-r` (the default value is also 2).

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv mcl -e 2 -r 2
```

This implementation is not optimized, so the processing of large graphs will likely be quite slow. So, for large graphs it is recommended to use the original implementation of the [MCL](https://micans.org/mcl/) algorithm, which is written in C and thus is really fast.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv mcl-bin -r 2 --bin /path/to/mcl
```

### Watset

[Watset] is a *local-global meta-algorithm* for fuzzy graph clustering. It builds an intermediate undirected graph that addresses the element ambiguity by considering different senses of each element in the input graph. This package offers the best-performing variation of Watset known as Simplified Watset.

Watset requires two different clustering algorithms to be selected: the *local* clustering algorithm for sense induction that is specified using `-l` (`--local`) and the *global* algorithm for the final clustering that is specified using `-g` (`--global`). It is possible to configure the algorithms using the similar options as shown above using the `-lp` (`--local-params`) and `-gp` (`--global-params`) options, which can be inserted multiple times.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv watset -l mcl -lp e=1 -lp r=3 -g cw -gp mode=lin
```

In practice, the default parameters for MCL and CW work well enough, so the `-lp` and `-gp` arguments can be omitted in this example.

#### Watset: Word Sense Induction

Since [Watset] performs curvature-based word sense induction, it is possible to extract the built sense inventory using the special command of this tool.

```bash
$ java -jar watset.jar -i graph.txt -o inventory.tsv senses -l mcl
```

The output of this operation is a tab-separated file containing three columns: the target word, the word sense identifier, and the set of related disambiguated senses in the given word sense (the *context*). Each sense is represented as `word#id`.

#### Watset: Word Sense Graph

It is also possible to print the intermediate word sense graph built by [Watset].

```bash
$ java -jar watset.jar -i graph.txt -o sense-graph.txt graph -l mcl
```

The output file will be written virtually in the same ABC format as the input graph, but each node will be provided with a numerical sense identifier preceded by the suffix `#`. This feature simplifies the integration into other graph processing pipelines.

#### Watset: Word Sense Graph Embeddings

It might be useful to obtain spectral embeddings of the [Watset] sense graph nodes in the Word2Vec textual format.

```
$ java -jar watset.jar -i graph.txt -o sense-graph.w2v embedsenses -l mcl -k 2
```

### MaxMax

[MaxMax] is a soft clustering algorithm for undirected graphs that constructs intermediate representation of the input graph as the directed graph. This algorithm has no options.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv maxmax
```

### Evaluation

This package provides implementations of two fuzzy clustering evaluation approaches: pairwise F-score and normalized modified purity. Given the set of clusters to evaluate and the set of gold clusters (&ldquo;classes&rdquo;), it is possible to study the clustering quality.

```
$ java -jar watset.jar -i output.tsv pairwise -g gold.tsv
$ java -jar watset.jar -i output.tsv purity -g gold.tsv -n -m
```

## Citation

* [Ustalov, D.](https://github.com/dustalov), [Panchenko, A.](https://github.com/alexanderpanchenko), [Biemann, C.](https://www.inf.uni-hamburg.de/en/inst/ab/lt/people/chris-biemann.html), [Ponzetto, S.P.](https://www.uni-mannheim.de/dws/people/professors/prof-dr-simone-paolo-ponzetto/): [Watset: Local-Global Graph Clustering with Applications in Sense and Frame Induction](https://doi.org/10.1162/COLI_a_00354). Computational Linguistics 45(3), 423&ndash;479 (2019)

```bibtex
@article{Ustalov:19:cl,
  author    = {Ustalov, Dmitry and Panchenko, Alexander and Biemann, Chris and Ponzetto, Simone Paolo},
  title     = {{Watset: Local-Global Graph Clustering with Applications in Sense and Frame Induction}},
  journal   = {Computational Linguistics},
  year      = {2019},
  volume    = {45},
  number    = {3},
  pages     = {423--479},
  doi       = {10.1162/COLI_a_00354},
  publisher = {MIT Press},
  issn      = {0891-2017},
  language  = {english},
}
```

## Tutorial

Watset will be featured at the [Graphs, Computation, and Language](https://doi.org/10.5281/zenodo.4291120) class at the [33rd European Summer School in Logic, Language and Information](https://2022.esslli.eu/courses-wokshops-accepted/accepted.html) (ESSLLI 2022)!

## Contributing

1. Fork it;
2. Create your feature branch (`git checkout -b my-new-feature`);
3. Commit your changes (`git commit -am 'Added some feature'`);
4. Push to the branch (`git push origin my-new-feature`);
5. Create new Pull Request.

## Copyright

Copyright (c) 2016&ndash;2022 [Dmitry Ustalov]. See [LICENSE](LICENSE) for details.

[Watset]: https://doi.org/10.1162/COLI_a_00354
[Chinese Whispers]: https://doi.org/10.3115/1654758.1654774
[Markov Clustering]: https://hdl.handle.net/1874/848
[MaxMax]: https://doi.org/10.1007/978-3-642-37247-6_30
[Dmitry Ustalov]: https://github.com/dustalov
