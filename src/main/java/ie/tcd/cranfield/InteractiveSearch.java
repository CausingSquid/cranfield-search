package ie.tcd.cranfield;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

final class InteractiveSearch {
    private static final int DEMO_K = 10;

    private InteractiveSearch() {
    }

    static void run(Path indexPath, Analyzer analyzer, Similarity similarity, float titleBoost) throws Exception {
        MultiFieldQueryParser parser = SearchConfig.buildParser(analyzer, titleBoost);

        try (Directory dir = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(dir);
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);
            StoredFields storedFields = searcher.storedFields();

            while (true) {
                System.out.print("\nquery> ");
                System.out.flush();
                String line = in.readLine();
                if (line == null || line.trim().equals(":q")) {
                    break;
                }
                if (line.isBlank()) {
                    continue;
                }

                Query query = SearchConfig.parseQuery(parser, line);
                TopDocs top = searcher.search(query, DEMO_K);
                System.out.println("Parsed query: " + query);
                System.out.println(top.totalHits.value() + " matching documents, top " + top.scoreDocs.length + ":");

                for (int rank = 0; rank < top.scoreDocs.length; rank++) {
                    ScoreDoc hit = top.scoreDocs[rank];
                    Document d = storedFields.document(hit.doc);
                    String title = d.get("title");
                    if (title.length() > 90) {
                        title = title.substring(0, 87) + "...";
                    }
                    System.out.printf(Locale.ROOT, "%2d. doc %-4s  score %7.4f  %s%n",
                            rank + 1, d.get("id"), hit.score, title);
                }
            }
        }
    }
}
