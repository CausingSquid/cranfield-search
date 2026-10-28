package ie.tcd.cranfield;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

final class Indexer {
    private Indexer() {
    }

    static void buildIndex(List<Map<String, String>> docs, Path indexPath,
                           Analyzer analyzer, Similarity similarity) throws IOException {
        Files.createDirectories(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setSimilarity(similarity);

        try (Directory dir = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(dir, config)) {
            for (Map<String, String> d : docs) {
                Document doc = new Document();
                doc.add(new StringField("id", d.get("I"), Field.Store.YES));
                doc.add(new TextField("title", d.getOrDefault("T", ""), Field.Store.YES));
                doc.add(new TextField("author", d.getOrDefault("A", ""), Field.Store.YES));
                doc.add(new TextField("content", d.getOrDefault("W", ""), Field.Store.YES));
                writer.addDocument(doc);
            }
            System.out.println("Indexed " + writer.getDocStats().numDocs + " documents into " + indexPath);
        }
    }
}
