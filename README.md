# Watset (Java)

This is an open source Java implementation of the [Watset](https://doi.org/10.18653/v1/P17-1145) graph-based soft clustering algorithm that is designed for synset induction.

This package also includes the implementation of the [Chinese Whispers](https://dl.acm.org/citation.cfm?id=1654774) (2006) and [MaxMax](https://doi.org/10.1007/978-3-642-37247-6_30) (2013) graph clustering algorithms.

[![Build Status][travis_ci_badge]][travis_ci_link]

[travis_ci_badge]: https://travis-ci.org/dustalov/watset-java.svg
[travis_ci_link]: https://travis-ci.org/dustalov/watset-java

## Usage

After cloning the repository, it is sufficient to use `mvn package` to produce the JAR file. The input undirected weighted graph should be represented in the text file, each line of which has three tab-separated fields: <code>word1&#9;word2&#9;weight</code>.

```bash
java -jar target/watset.jar -method watset -in input.txt -out output.txt
```

Each line of the output file has three tab-separated fields: the number of the cluster, the size of the cluster, and the list of the words belonging to the cluster separated with comma and space.

It is also possible to use Watset as a library by calling the `Watset` class.

## Contributing

1. Fork it;
2. Create your feature branch (`git checkout -b my-new-feature`);
3. Commit your changes (`git commit -am 'Added some feature'`);
4. Push to the branch (`git push origin my-new-feature`);
5. Create new Pull Request.

## Copyright

Copyright (c) 2016-2017 [Dmitry Ustalov]. See LICENSE for details.

[Dmitry Ustalov]: https://ustalov.com/
