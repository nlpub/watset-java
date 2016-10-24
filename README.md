# MaxMax

This is an open source Java implementation of the MaxMax graph-based soft clustering algorithm that is designed for word sense induction. The algorithm is proposed by [Hope & Keller](http://dx.doi.org/10.1007/978-3-642-37247-6_30) (2013).

[![Build Status][travis_ci_badge]][travis_ci_link]

[travis_ci_badge]: https://travis-ci.org/dustalov/maxmax.svg
[travis_ci_link]: https://travis-ci.org/dustalov/maxmax

## Usage

After cloning the repository, it is sufficient to use `mvn package` to produce the JAR file. The input undirected weighted graph should be represented in the text file, each line of which has three tab-separated fields: <code>word1&#9;word2&#9;weight</code>.

```bash
java -jar target/maxmax.jar -in input.txt -out output.txt
```

Each line of the output file has three tab-separated fields: the number of the cluster, the size of the cluster, and the list of the words belonging to the cluster separated with comma and space.

It is also possible to use MaxMax as a library by calling the `MaxMax` class.

## Contributing

1. Fork it;
2. Create your feature branch (`git checkout -b my-new-feature`);
3. Commit your changes (`git commit -am 'Added some feature'`);
4. Push to the branch (`git push origin my-new-feature`);
5. Create new Pull Request.

## Copyright

Copyright (c) 2016 [Dmitry Ustalov]. See LICENSE for details.

[Dmitry Ustalov]: https://ustalov.name/
