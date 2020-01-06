#!/usr/bin/env python3

import networkx as nx
import pickle

if __name__ == '__main__':
    G = nx.karate_club_graph()

    nx.write_gpickle(G, 'karate_club_graph.pkl', protocol=3)
