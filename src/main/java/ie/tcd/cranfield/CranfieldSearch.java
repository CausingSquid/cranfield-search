package ie.tcd.cranfield;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.Similarity;

public class CranfieldSearch {
    private static final int DEFAULT_DEPTH = 50;

    public static void main(String[] args) throws Exception {
        if (args.length >= 4 && args[0].equalsIgnoreCase("interactive")) {
            runInteractive(args);
            return;
        }
        if (args.length < 4) {
            System.err.println("Usage: java -jar cranfield-search.jar <cranDir> <english|standard> "
                    + "<vsm|bm25|lmd> <resultsFile> [titleBoost] [depth]");
            System.err.println("   or: java -jar cranfield-search.jar interactive <cranDir> <english|standard> "
                    + "<vsm|bm25|lmd> [titleBoost]");
            System.exit(1);
        }
        Path cranDir = Paths.get(args[0]);
        String analyzerName = args[1].toLowerCase(Locale.ROOT);
        String simName = args[2].toLowerCase(Locale.ROOT);
        Path resultsFile = Paths.get(args[3]);
        float titleBoost = args.length > 4 ? Float.parseFloat(args[4]) : 1.0f;
        int depth = args.length > 5 ? Integer.parseInt(args[5]) : DEFAULT_DEPTH;

        Analyzer analyzer = SearchConfig.buildAnalyzer(analyzerName);
        Similarity similarity = SearchConfig.buildSimilarity(simName);
        String runTag = analyzerName + "_" + simName;
        Path indexPath = Paths.get("index", runTag);

        List<Map<String, String>> docs = CranfieldParser.parse(cranDir.resolve("cran.all.1400"));
        System.out.println("Parsed " + docs.size() + " documents");
        Indexer.buildIndex(docs, indexPath, analyzer, similarity);

        List<Map<String, String>> queries = CranfieldParser.parse(cranDir.resolve("cran.qry"));
        System.out.println("Parsed " + queries.size() + " queries");
        BatchSearcher.runQueries(queries, indexPath, analyzer, similarity, titleBoost, depth, resultsFile, runTag);

        System.out.println("Results written to " + resultsFile);
    }

    private static void runInteractive(String[] args) throws Exception {
        Path cranDir = Paths.get(args[1]);
        String analyzerName = args[2].toLowerCase(Locale.ROOT);
        String simName = args[3].toLowerCase(Locale.ROOT);
        float titleBoost = args.length > 4 ? Float.parseFloat(args[4]) : 1.0f;

        Analyzer analyzer = SearchConfig.buildAnalyzer(analyzerName);
        Similarity similarity = SearchConfig.buildSimilarity(simName);
        Path indexPath = Paths.get("index", analyzerName + "_" + simName);
        Indexer.buildIndex(CranfieldParser.parse(cranDir.resolve("cran.all.1400")), indexPath, analyzer, similarity);

        System.out.println("Searching with " + analyzerName + " analyzer + " + simName
                + " scoring. Type a query, or :q to quit.");
        InteractiveSearch.run(indexPath, analyzer, similarity, titleBoost);
    }
}
