# Watset

This is an open source implementation of the [Watset] algorithm for fuzzy graph clustering (aka [soft clustering](https://en.wikipedia.org/wiki/Soft_clustering)).

This package, written in Java, also includes implementations of the [Chinese Whispers] (2006), [Markov Clustering], and [MaxMax] (2013) graph clustering algorithms.

[![Build Status][travis_ci_badge]][travis_ci_link]

[travis_ci_badge]: https://travis-ci.org/nlpub/watset-java.svg
[travis_ci_link]: https://travis-ci.org/nlpub/watset-java

## Usage

There are two ways to obtain `watset-java`:

1. to download the [recently released version](https://github.com/nlpub/watset-java/releases);

2. to compile the `master` branch from source by cloning the [repository](https://github.com/nlpub/watset-java) and running `mvn package` in the repository root.

This tool has five kinds of activity named the *commands*: four invoke different clustering algorithms (`watset`, `cw`, `mcl`, `maxmax`), one invokes graph-based word sense induction (`senses`).

There are two global command-line arguments: `-i` (or `--input`) that specifies the path of input file and `-o` (or `--output`) that specifies the path of the output file. The default values for these parameters are `/dev/stdin` and `/dev/stdout`, correspondingly.

The input for this tool is an undirected weighted graph represented in the ABC format. This format is a tab-separated edge list: <code>word1&#9;word2&#9;weight</code>. The output of the graph clustering algorithm is a tab-separated file containing three columns: the cluster identifier, the cluster size, and the list of elements belonging to the cluster.

In fact, `watset-java` is not just a tool, it also features a complete API for graph clustering. This API can easily be embedded into your application or library.

### Chinese Whispers

[Chinese Whispers] is a hard clustering algorithm that resembles the popular children's game. This tool offers four different modes of this algorithm that can be set using the `-m` (`--mode`) option:

* `chris`: the originally proposed node selection approach in Chinese Whispers that is suitable even for unweighted graphs;
* `top`: the node selection approach that chooses for each node the neighboring node with the maximal edge weight;
* `nolog`: the `top` mode divided by the node degree;
* `log`: the `top` mode divided by the logarithm of the node degree.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv cw -m top
```

### Markov Clustering

[Markov Clustering] is a hard clustering algorithm that simulates random walks on the graph. It is possible to specify two options of this algorithm:

* the number of expansion operations `-e` (the default value is 2);
* the power coefficient `-r` (the default value is also 2).

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv mcl -e 2 -r 2
```

This implementation is not optimized, so the processing of large graphs will likely be quite slow. For that, it is recommended to use the original implementation of this [MCL](https://micans.org/mcl/) algorithm, which is written in C and thus really fast.

### Watset

[Watset] is a *local-global meta-algorithm* for fuzzy graph clustering. It builds an intermediate undirected graph that addresses the element ambiguity by considering different senses of each element in the input graph.

Watset requires two different clustering algorithms to be selected: the *local* clustering algorithm for sense induction that is specified using `-l` (`--local`) and the *global* algorithm for the final clustering that is specified using `-g` (`--global`). It is possible to configure the algorithms using the similar options as shown above using the `-lp` (`--local-params`) and `-gp` (`--global-params`) options. Multiple parameters should be separated with ampersand.

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv watset -l mcl -lp e=1&r=3 -g cw -gp mode=nolog
```

In practice, the default parameters for MCL work well enough, so the `-lp` argument can be omitted in this example.

### Watset: Word Sense Induction

Since Watset performs curvature-based word sense induction, it is possible to extract the built sense inventory using the special command of this tool.

```bash
$ java -jar watset.jar -i graph.txt -o inventory.tsv senses -l mcl
```

The output of this operation is a tab-separated file containing three columns: the target word, the word sense identifier, and the set of related words in the given word sense (the *context*).

### MaxMax

[MaxMax] is a soft clustering algorithm for undirected graphs that constructs intermediate representation of the input graph as the directed graph. This algorithms has no options. 

```bash
$ java -jar watset.jar -i graph.txt -o output.tsv maxmax
```

## Citation

* [Ustalov, D.](https://github.com/dustalov), [Panchenko, A.](https://www.inf.uni-hamburg.de/en/inst/ab/lt/people/alexander-panchenko.html), [Biemann, C.](https://www.inf.uni-hamburg.de/en/inst/ab/lt/people/chris-biemann.html): [Watset: Automatic Induction of Synsets from a Graph of Synonyms](https://doi.org/10.18653/v1/P17-1145). In: Proceedings of the 55th Annual Meeting of the Association for Computational Linguistics (Volume 1: Long Papers), Vancouver, Canada, Association for Computational Linguistics (2017) 1579–1590

```latex
@inproceedings{Ustalov:17:acl,
  author    = {Ustalov, Dmitry and Panchenko, Alexander and Biemann, Chris},
  title     = {{Watset: Automatic Induction of Synsets from a Graph of Synonyms}},
  booktitle = {Proceedings of the 55th Annual Meeting of the Association for Computational Linguistics (Volume 1: Long Papers)},
  year      = {2017},
  pages     = {1579--1590},
  doi       = {10.18653/v1/P17-1145},
  address   = {Vancouver, Canada},
  publisher = {Association for Computational Linguistics},
  language  = {english},
}
```

## Contributing

1. Fork it;
2. Create your feature branch (`git checkout -b my-new-feature`);
3. Commit your changes (`git commit -am 'Added some feature'`);
4. Push to the branch (`git push origin my-new-feature`);
5. Create new Pull Request.

## Copyright

Copyright (c) 2016-2018 [Dmitry Ustalov]. See LICENSE for details.

[Watset]: https://doi.org/10.18653/v1/P17-1145
[Chinese Whispers]: https://dl.acm.org/citation.cfm?id=1654774
[Markov Clustering]: https://doi.org/10.1137/040608635
[MaxMax]: https://doi.org/10.1007/978-3-642-37247-6_30
[Dmitry Ustalov]: https://ustalov.com/
