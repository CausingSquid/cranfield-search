# Cranfield search engine (CS7IS3 Assignment 1)

Name : Ashwin Sathyanarayanan Astapatri

Student ID : 26334122

Lucene 10.3.0 search engine for the Cranfield corpus (1400 documents, 225 queries),
evaluated with trec_eval.

## Layout

```
~/cranfield-search/        this project (Maven)
  src/main/java/ie/tcd/cranfield/
    CranfieldSearch.java     entry point, command-line arguments
    CranfieldParser.java     parses the .I/.T/.A/.B/.W format
    SearchConfig.java        analyzers, similarities, query parser
    Indexer.java             builds the Lucene index
    BatchSearcher.java       runs the 225 queries, writes the TREC run file
    InteractiveSearch.java   interactive demo mode
  run_all.sh               runs and evaluates every configuration
  results/                 run files, qrels.txt, summary.txt
  index/                   Lucene indexes, one per analyzer + similarity
~/cranfield/               cran.all.1400  cran.qry  cranqrel
~/trec_eval/trec_eval      evaluation tool
```

## Requirements

- Java 21 or later
- Maven
- trec_eval, built in `~/trec_eval`

On a fresh Ubuntu machine like in an azure VM, install Java, Maven and a C compiler for trec_eval:

```bash
sudo apt update && sudo apt install -y openjdk-21-jdk maven build-essential
```

Then build trec_eval:

```bash
cd ~ && wget https://trec.nist.gov/trec_eval/trec_eval_latest.tar.gz
tar xzf trec_eval_latest.tar.gz && mv trec_eval-* trec_eval && cd trec_eval && make
```

## Dataset

Download the Cranfield collection from http://ir.dcs.gla.ac.uk/resources/test_collections/cran/
and place `cran.all.1400`, `cran.qry` and `cranqrel` in `~/cranfield/`:

```bash
ls ~/cranfield    # cran.all.1400  cran.qry  cranqrel
```

## Compile

```bash
cd ~/cranfield-search
mvn package
```

This produces `target/cranfield-search.jar`, which includes all of its dependencies.

## Run one configuration

```bash
java -jar target/cranfield-search.jar <cranDir> <english|standard> <vsm|bm25|lmd> <resultsFile> [titleBoost] [depth]
```

| Argument | Meaning |
|---|---|
| `english` / `standard` | EnglishAnalyzer (stop words + Porter stemming) or StandardAnalyzer |
| `vsm` / `bm25` / `lmd` | ClassicSimilarity (TF-IDF), BM25 (k1=1.2, b=0.75), LM Dirichlet (mu=2000) |
| `titleBoost` | weight of the title field relative to the abstract, default 1.0 |
| `depth` | results kept per query, default 50; use 1000 for the standard TREC depth |

The program rebuilds the index and writes a TREC run file (`qid Q0 docid rank score tag`).
The queries are numbered 1..225 by their position in `cran.qry` so that they match the qrels.

Example:

```bash
java -jar target/cranfield-search.jar ~/cranfield english bm25 results/english_bm25_d50.txt
java -jar target/cranfield-search.jar ~/cranfield english bm25 results/english_bm25_d1000.txt 1.0 1000
```

## Evaluate

trec_eval requires qrels with four columns (`qid 0 docid relevance`), where the larger numbers are more relevant. The Cranfield grading scale ranges from 1 (most relevant) to 4, thus it is inverted during conversion:
Cranfield grades run from 1 (most relevant) to 4, so they are inverted when converted:

```bash
mkdir -p results
awk '{g = ($3 > 0) ? 5 - $3 : 0; print $1, 0, $2, g}' ~/cranfield/cranqrel > results/qrels.txt
~/trec_eval/trec_eval -m map -m P.5 -m recall.50,1000 -m ndcg_cut.10 results/qrels.txt results/english_bm25_d50.txt
```

## Run everything

```bash
./run_all.sh 2>/dev/null | tee results/summary.txt
```

It runs both the analyzers using all three similarity measures with depth values of 50 and 1000, and also includes a title boost sweep between 0.5 and 2.0 for English + BM25. It only takes a few minutes to execute. The 2>/dev/null statement ensures that any unnecessary Lucene warnings are not displayed.

## Interactive search

```bash
java -jar target/cranfield-search.jar interactive ~/cranfield english bm25
```

Type a query to see the top 10 documents. Type `:q` to quit.

## Results (MAP)

| Configuration | depth 50 | depth 1000 |
|---|---|---|
| English + BM25 | 0.2979 | 0.3107 |
| English + VSM | 0.2709 | 0.2838 |
| English + LMD | 0.2396 | 0.2533 |
| Standard + BM25 | 0.2721 | 0.2848 |
| Standard + VSM | 0.2181 | 0.2309 |
| Standard + LMD | 0.2220 | 0.2358 |

The full metrics are in `results/summary.txt`.
