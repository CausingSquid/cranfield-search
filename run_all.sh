#!/usr/bin/env bash
set -e

CRAN=~/cranfield
TREC=~/trec_eval/trec_eval
JAR=target/cranfield-search.jar
DEPTHS="50 1000"

mkdir -p results

awk '{g = ($3 > 0) ? 5 - $3 : 0; print $1, 0, $2, g}' "$CRAN/cranqrel" > results/qrels.txt

for depth in $DEPTHS; do
  echo "################ depth $depth (results kept per query) ################"
  for analyzer in english standard; do
    for sim in vsm bm25 lmd; do
      out="results/${analyzer}_${sim}_d${depth}.txt"
      java -jar "$JAR" "$CRAN" "$analyzer" "$sim" "$out" 1.0 "$depth" > /dev/null
      echo "=== $analyzer + $sim (depth $depth) ==="
      "$TREC" -m map -m P.5 -m recall.50,1000 -m ndcg_cut.10 results/qrels.txt "$out"
    done
  done
done

for depth in $DEPTHS; do
  for boost in 0.5 0.75 1.0 1.5 2.0; do
    out="results/english_bm25_title${boost}_d${depth}.txt"
    java -jar "$JAR" "$CRAN" english bm25 "$out" "$boost" "$depth" > /dev/null
    echo "=== english + bm25, title boost $boost (depth $depth) ==="
    "$TREC" -m map -m ndcg_cut.10 results/qrels.txt "$out"
  done
done
