# Watset Examples

Code in this directory shows API examples of the Watset implementation in Java. Make sure that [Groovy](http://www.groovy-lang.org/) and [Python 3](https://www.python.org/) are installed. For the convenience of the user, `Makefile` wraps the commands.

## Hard Clustering Algorithms

[HardClustering.groovy](HardClustering.groovy) runs the empty clustering, singleton clustering, together clustering, connected components clustering, Chinese Whispers, and Markov Clustering algorithms.

```shell
make HardClustering
```

## Fuzzy Clustering Algorithms

[FuzzyClustering.groovy](FuzzyClustering.groovy) runs the MaxMax and Watset fuzzy clustering algorithms.

```shell
make FuzzyClustering
```

## Interoperation with Python via Pickle

[PickleClustering.groovy](PickleClustering.groovy) runs the Chinese Whispers clustering algorithm on a Karate Club graph produced by [PickleGraph.py](PickleGraph.py).

```shell
make PickleGraph PickleClustering
```

## Command-Line Interface

[CommandLine.sh](CommandLine.sh) shows various features of the Watset command-line interface. As the input the graph [java.tsv](java.tsv) that is similar to the one shown in Figure&nbsp;16 in the [Watset article](https://doi.org/10.1162/coli_a_00354) is used.

```shell
make CommandLine
```
