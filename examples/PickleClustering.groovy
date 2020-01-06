#!/usr/bin/env groovy

// python3 PickleGraph.py
// groovy -classpath watset.jar PickleClustering.groovy

@Grab('net.razorvine:pyrolite:4.30')
import net.razorvine.pickle.Unpickler
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import org.nlpub.watset.graph.ChineseWhispers
import org.nlpub.watset.graph.NodeWeighting

builder = SimpleWeightedGraph.<Integer, DefaultWeightedEdge> createBuilder(DefaultWeightedEdge.class)

pickle = new File('karate_club_graph.pkl').withInputStream { stream ->
    new Unpickler().load(stream)
}

assert 'networkx.classes.graph.Graph' == pickle.__class__, 'The example might be outdated'

print('Graph: ')
println(pickle.graph.name)

pickle._node.each { node ->
    builder.addVertex(node.key)
}

pickle._adj.each { source ->
    source.value.each { target ->
        builder.addEdge(source.key, target.key)
    }
}

graph = builder.build()

assert 34 == graph.vertexSet().size(), 'The example might be outdated'
assert 78 == graph.edgeSet().size(), 'The example might be outdated'

// Chinese Whispers Example
cw = new ChineseWhispers<>(graph, NodeWeighting.top(), 10, new Random(0))
cw.fit()

print('Chinese Whispers Clusters: ')
println(cw.getClusters())
