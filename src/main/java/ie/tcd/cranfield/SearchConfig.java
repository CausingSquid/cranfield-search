package ie.tcd.cranfield;

import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

final class SearchConfig {
    private SearchConfig() {
    }

    static Analyzer buildAnalyzer(String name) {
        return switch (name) {
            case "english" -> new EnglishAnalyzer();
            case "standard" -> new StandardAnalyzer();
            default -> throw new IllegalArgumentException("Unknown analyzer: " + name);
        };
    }

    static Similarity buildSimilarity(String name) {
        return switch (name) {
            case "vsm" -> new ClassicSimilarity();
            case "bm25" -> new BM25Similarity(1.2f, 0.75f);
            case "lmd" -> new LMDirichletSimilarity();
            default -> throw new IllegalArgumentException("Unknown similarity: " + name);
        };
    }

    static MultiFieldQueryParser buildParser(Analyzer analyzer, float titleBoost) {
        Map<String, Float> boosts = Map.of("title", titleBoost, "content", 1.0f);
        return new MultiFieldQueryParser(new String[] {"title", "content"}, analyzer, boosts);
    }

    static Query parseQuery(MultiFieldQueryParser parser, String text) throws Exception {
        return parser.parse(QueryParser.escape(text.toLowerCase(Locale.ROOT)));
    }
}
