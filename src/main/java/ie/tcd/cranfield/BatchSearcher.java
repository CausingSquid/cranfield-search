package ie.tcd.cranfield;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

final class BatchSearcher {
    private BatchSearcher() {
    }

    static void runQueries(List<Map<String, String>> queries, Path indexPath,
                           Analyzer analyzer, Similarity similarity, float titleBoost, int depth,
                           Path resultsFile, String runTag) throws Exception {
        MultiFieldQueryParser parser = SearchConfig.buildParser(analyzer, titleBoost);

        try (Directory dir = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(dir);
             PrintWriter out = new PrintWriter(Files.newBufferedWriter(resultsFile))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);
            StoredFields storedFields = searcher.storedFields();

            int queryNumber = 0;
            for (Map<String, String> q : queries) {
                queryNumber++;
                Query query = SearchConfig.parseQuery(parser, q.getOrDefault("W", ""));

                ScoreDoc[] hits = searcher.search(query, depth).scoreDocs;
                for (int rank = 0; rank < hits.length; rank++) {
                    String docId = storedFields.document(hits[rank].doc).get("id");
                    out.printf(Locale.ROOT, "%d Q0 %s %d %.6f %s%n",
                            queryNumber, docId, rank + 1, hits[rank].score, runTag);
                }
            }
        }
    }
}
