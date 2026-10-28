# Lab demo commands – Cranfield Lucene search engine

`2>/dev/null` on the java commands hides Lucene's harmless "vector incubator" warnings.

## 0. Open the environment
```bash
wsl -d Ubuntu-24.04
sudo su
cd ~ && ls          # apache-lucene-demo  cranfield  cranfield-search  trec_eval
```

## 1. Show the setup (versions + dataset)
```bash
java -version
mvn -version
grep lucene.version ~/cranfield-search/pom.xml          # 10.3.0
ls ~/cranfield                                          # cran.all.1400  cran.qry  cranqrel ...
grep -c '^\.I' ~/cranfield/cran.all.1400                # 1400 documents
grep -c '^\.I' ~/cranfield/cran.qry                     # 225 queries
head -20 ~/cranfield/cran.all.1400                      # .I .T .A .B .W format
head -5 ~/cranfield/cranqrel                            # qid docid grade
```

## 2. Build
```bash
cd ~/cranfield-search
mvn package
```

## 3. Walk through the code
```bash
ls src/main/java/ie/tcd/cranfield/
cd src/main/java/ie/tcd/cranfield
less CranfieldSearch.java      # entry point: reads the arguments, wires the steps together
less CranfieldParser.java      # .I/.T/.A/.B/.W file parser
less SearchConfig.java         # analyzers, similarities, multi-field query parser
less Indexer.java              # fields: id, title, author, content
less BatchSearcher.java        # 225 queries, numbered by position, TREC output
less InteractiveSearch.java    # demo mode
cd ~/cranfield-search
cat run_all.sh
```
Point out: the parser, the index fields, analyzers, similarities,
query numbering 1..225 by position, results depth (default 50, optional 1000), grade inversion in run_all.sh.

## 4. One full run (index + 225 queries) and evaluate it
Arguments: `<cranDir> <analyzer> <similarity> <resultsFile> [titleBoost] [depth]` (depth defaults to 50).
```bash
java -jar target/cranfield-search.jar ~/cranfield english bm25 results/english_bm25_d50.txt 2>/dev/null
head -5 results/english_bm25_d50.txt    # TREC run format: qid Q0 docid rank score tag
head -5 results/qrels.txt               # qid 0 docid relevance
~/trec_eval/trec_eval -m map -m P.5 -m recall.50 -m ndcg_cut.10 results/qrels.txt results/english_bm25_d50.txt
```
Same run keeping 1000 results per query (standard TREC depth):
```bash
java -jar target/cranfield-search.jar ~/cranfield english bm25 results/english_bm25_d1000.txt 1.0 1000 2>/dev/null
~/trec_eval/trec_eval -m map -m recall.1000 results/qrels.txt results/english_bm25_d1000.txt
```

## 5. All configurations (VSM vs BM25 vs LMD, English vs Standard, depth 50 and 1000)
Takes a few minutes, so show the saved results first:
```bash
cat results/summary.txt
```
Re-run everything live only if asked:
```bash
./run_all.sh 2>/dev/null | tee results/summary.txt
```

## 6. Interactive search (the "functioning search engine" demo)
```bash
java -jar target/cranfield-search.jar interactive ~/cranfield english bm25 2>/dev/null
```
Queries to type (`:q` to quit):
- `boundary layer transition`
- `what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft` (Cranfield query 1)
- `the effects of heat transfer on supersonic flow`

Then the same query on the baseline, to show the analyzer difference
(parsed query keeps "the"/"of" and has no stemming):
```bash
java -jar target/cranfield-search.jar interactive ~/cranfield standard vsm 2>/dev/null
```

## 7. Check results against the relevance judgements (query 1)
```bash
awk 'NR==FNR{if($1==1 && $4>0) rel[$3]=$4; next}
     $1==1 && $4<=10 {print "rank", $4, "doc", $3, ($3 in rel ? "<- RELEVANT (grade " rel[$3] ")" : "")}' \
    results/qrels.txt results/english_bm25_d50.txt
```
(grades here are inverted: 4 = most relevant)

## 8. Per-query analysis (if asked which queries do badly)
```bash
~/trec_eval/trec_eval -q -m map results/qrels.txt results/english_bm25_d50.txt | sort -k3 -n | head -10
```
