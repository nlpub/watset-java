#!/usr/bin/env groovy

// groovy -classpath watset.jar HardClustering.groovy

import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import org.nlpub.watset.graph.*

graph = SimpleWeightedGraph.<String, DefaultWeightedEdge> createBuilder(DefaultWeightedEdge.class).
        addVertices('a', 'b', 'c', 'd', 'e').
        addEdge('a', 'b').
        addEdge('a', 'c').
        addEdge('a', 'c').
        addEdge('d', 'e').
        build();

print('Graph: ')
println(graph)

// Empty Example
empty = new EmptyClustering<>()
empty.fit()

print('Empty Clusters: ')
println(empty.getClusters())

// Singleton Example
singleton = new SingletonClustering<>(graph)
singleton.fit()

print('Singleton Clusters: ')
println(singleton.getClusters())

// Together Example
together = new TogetherClustering<>(graph)
together.fit()

print('Together Clusters: ')
println(together.getClusters())

// Components Clustering Example
components = new ComponentsClustering<>(graph)
components.fit()

print('Components Clusters: ')
println(components.getClusters())

// Chinese Whispers Example
cw = new ChineseWhispers<>(graph, NodeWeighting.top())
cw.fit()

print('Chinese Whispers Clusters: ')
println(cw.getClusters())

// Markov Clustering Example
mcl = new MarkovClustering<>(graph, 2, 2)
mcl.fit()

print('Markov Clustering Clusters: ')
println(mcl.getClusters())
