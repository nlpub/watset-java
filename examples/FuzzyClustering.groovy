#!/usr/bin/env groovy

// groovy -classpath watset.jar FuzzyClustering.groovy

import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import org.nlpub.watset.graph.ChineseWhispers
import org.nlpub.watset.graph.MaxMax
import org.nlpub.watset.graph.NodeWeighting
import org.nlpub.watset.graph.SimplifiedWatset

graph = SimpleWeightedGraph.<String, DefaultWeightedEdge> createBuilder(DefaultWeightedEdge.class).
        addVertices('a', 'b', 'c', 'd', 'e').
        addEdge('a', 'b').
        addEdge('a', 'c').
        addEdge('a', 'c').
        addEdge('d', 'e').
        build()

print('Graph: ')
println(graph)

// MaxMax Example
maxmax = new MaxMax<>(graph)
maxmax.fit()

print('MaxMax Digraph: ')
println(maxmax.getDigraph())

print('MaxMax Clusters: ')
println(maxmax.getClusters())

// Watset Example
local = ChineseWhispers.provider(NodeWeighting.top())
global = ChineseWhispers.provider(NodeWeighting.top())

watset = new SimplifiedWatset<>(graph, local, global)
watset.fit()

print('Watset Sense Graph: ')
println(watset.getSenseGraph())

print('Watset Clusters: ')
println(watset.getClusters())
