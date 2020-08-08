#!/bin/sh

WATSET=${WATSET-watset.jar}
INPUT=${INPUT-java.tsv}

set -ex

# Version
java -jar "$WATSET" -v

# Dummy Clustering
java -jar "$WATSET" -i "$INPUT" empty
java -jar "$WATSET" -i "$INPUT" singleton
java -jar "$WATSET" -i "$INPUT" together
java -jar "$WATSET" -i "$INPUT" components

# k Spanning Tree Clustering
java -jar "$WATSET" -i "$INPUT" kst -k 2

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
java -jar "$WATSET" -i "$INPUT" senses -s -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" senses -s -l cw -lp mode=$lmode
done

# Watset Sense Graph
java -jar "$WATSET" -i "$INPUT" graph -s -l mcl

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" graph -s -l cw -lp mode=$lmode
done

# Watset Clustering
java -jar "$WATSET" -i "$INPUT" watset -s -l mcl -g mcl # Watset[MCL, MCL]

[ -x "$PWD/mcl" ] && java -jar "$WATSET" -i "$INPUT" watset -s -l mcl -g mcl-bin -gp bin="$PWD/mcl" # Watset[MCL, MCL]

for gmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" watset -s -l mcl -g cw -gp mode=$gmode # Watset[MCL, CW]
done

for lmode in top lin log ; do
  java -jar "$WATSET" -i "$INPUT" watset -s -l cw -lp mode=$lmode -g mcl # Watset[CW, MCL]

  [ -x "$PWD/mcl" ] && java -jar "$WATSET" -i "$INPUT" watset -s -l cw -lp mode=$lmode -g mcl-bin -gp bin="$PWD/mcl" # Watset[CW, MCL]

  for gmode in top lin log ; do
    java -jar "$WATSET" -i "$INPUT" watset -s -l cw -lp mode=$lmode -g cw -gp mode=$gmode # Watset[CW, CW]
  done
done
