#!/bin/sh

WATSET=${WATSET-watset.jar}
INPUT=${INPUT-java.tsv}

set -ex

# Dummy Clustering
java -jar "$WATSET" empty -i "$INPUT"
java -jar "$WATSET" singleton -i "$INPUT"
java -jar "$WATSET" together -i "$INPUT"
java -jar "$WATSET" components -i "$INPUT"

# Chinese Whispers
for mode in top lin log ; do
  java -jar "$WATSET" cw -i "$INPUT" -m $mode
done

# Markov Clustering
java -jar "$WATSET" mcl -i "$INPUT"

# MaxMax
java -jar "$WATSET" maxmax -i "$INPUT"

# Watset Sense Induction
java -jar "$WATSET" senses -i "$INPUT" -s -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" senses -i "$INPUT" -s -l cw -lp mode=$lmode
done

# Watset Sense Graph
java -jar "$WATSET" graph -i "$INPUT" -s -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" graph -i "$INPUT" -s -l cw -lp mode=$lmode
done

# Watset Clustering
java -jar "$WATSET" watset -i "$INPUT" -s -l mcl -g mcl # Watset[MCL, MCL]

for gmode in top lin log ; do
  java -jar "$WATSET" watset -i "$INPUT" -s -l mcl -g cw -gp mode=$gmode # Watset[MCL, CW]
done

for lmode in top lin log ; do
  java -jar "$WATSET" watset -i "$INPUT" -s -l cw -lp mode=$lmode -g mcl # Watset[CW, MCL]

  for gmode in top lin log ; do
    java -jar "$WATSET" watset -i "$INPUT" -s -l cw -lp mode=$lmode -g cw -gp mode=$gmode # Watset[CW, CW]
  done
done
