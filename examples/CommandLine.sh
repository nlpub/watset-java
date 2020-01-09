#!/bin/sh

WATSET=${WATSET-watset.jar}
INPUT=${INPUT-java.tsv}

set -ex

# Chinese Whispers
for mode in top lin log ; do
  java -jar $WATSET -i $INPUT cw -m $mode
done

# Markov Clustering
java -jar $WATSET -i $INPUT mcl

# MaxMax
java -jar $WATSET -i $INPUT maxmax

# Watset Sense Induction
java -jar $WATSET -i $INPUT senses -s -l mcl

for lmode in top lin log ; do
  java -jar $WATSET -i $INPUT senses -s -l cw -lp mode=$lmode
done

# Watset Sense Graph
java -jar $WATSET -i $INPUT graph -s -l mcl

for lmode in top lin log ; do
  java -jar $WATSET -i $INPUT graph -s -l cw -lp mode=$lmode
done

# Watset Clustering
java -jar $WATSET -i $INPUT watset -s -l mcl -g mcl # Watset[MCL, MCL]

for gmode in top lin log ; do
  java -jar $WATSET -i $INPUT watset -s -l mcl -g cw -gp mode=$gmode # Watset[MCL, CW]
done

for lmode in top lin log ; do
  java -jar $WATSET -i $INPUT watset -s -l cw -lp mode=$lmode -g mcl # Watset[CW, MCL]

  for gmode in top lin log ; do
    java -jar $WATSET -i $INPUT watset -s -l cw -lp mode=$lmode -g cw -gp mode=$gmode # Watset[CW, CW]
  done
done
