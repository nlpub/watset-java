#!/bin/sh

WATSET=${WATSET-watset.jar}
INPUT=${INPUT-java.tsv}

set -eux

# Version
java -jar "$WATSET" -v

# Dummy Clustering
java -jar "$WATSET" -i "$INPUT" empty
java -jar "$WATSET" -i "$INPUT" singleton
java -jar "$WATSET" -i "$INPUT" together
java -jar "$WATSET" -i "$INPUT" components

# k Spanning Tree Clustering
java -jar "$WATSET" -i "$INPUT" kst -k 2

# Spectral Embedding
java -jar "$WATSET" -i "$INPUT" embed -k 2

# Spectral Clustering
java -jar "$WATSET" -i "$INPUT" spectral -k 2

# Chinese Whispers
for mode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" cw -m $mode
done

# Markov Clustering
java -jar "$WATSET" -i "$INPUT" mcl

# Markov Clustering (Official)
[ -x "$PWD/mcl" ] && java -jar "$WATSET" -i "$INPUT" mcl-bin --bin "$PWD/mcl"

# MaxMax
java -jar "$WATSET" -i "$INPUT" maxmax

# Watset Sense Induction
java -jar "$WATSET" -i "$INPUT" senses -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" senses -l cw -lp mode=$lmode
done

# Watset Sense Graph
java -jar "$WATSET" -i "$INPUT" graph -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" graph -l cw -lp mode=$lmode
done

# Watset Sense Graph Embedding
java -jar "$WATSET" -i "$INPUT" embedsenses -l mcl -k 2

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" embedsenses -l cw -lp mode=$lmode -k 2
done

# Watset Clustering
java -jar "$WATSET" -i "$INPUT" watset -l mcl -g mcl # Watset[MCL, MCL]

[ -x "$PWD/mcl" ] && java -jar "$WATSET" -i "$INPUT" watset -l mcl -g mcl-bin -gp bin="$PWD/mcl" # Watset[MCL, MCL]

for gmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" watset -l mcl -g cw -gp mode=$gmode # Watset[MCL, CW]
done

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" watset -l cw -lp mode=$lmode -g mcl # Watset[CW, MCL]

  [ -x "$PWD/mcl" ] && java -jar "$WATSET" -i "$INPUT" watset -l cw -lp mode=$lmode -g mcl-bin -gp bin="$PWD/mcl" # Watset[CW, MCL]

  for gmode in top lin log ; do
    java -jar "$WATSET" -i "$INPUT" watset -l cw -lp mode=$lmode -g cw -gp mode=$gmode # Watset[CW, CW]
  done
done

# Evaluation

java -jar "$WATSET" -i "bank-soft.tsv" pairwise -g "bank-gold.tsv"
java -jar "$WATSET" -i "bank-soft.tsv" purity -g "bank-gold.tsv" -n -m
