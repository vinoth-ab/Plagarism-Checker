import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes numeric similarity scores between two documents using two
 * complementary techniques:
 *
 *  - Cosine similarity over word-frequency vectors (good at catching
 *    documents that reuse the same vocabulary, regardless of order).
 *  - Jaccard similarity over word-shingle sets (good at catching
 *    directly copy-pasted phrases, since it respects word order).
 *
 * The two scores are blended into a single "overall" plagiarism score.
 */
public class SimilarityCalculator {

    /**
     * Cosine similarity between two word-frequency maps. Returns a
     * value between 0.0 (completely different) and 1.0 (identical
     * word-usage distribution).
     */
    public double cosineSimilarity(Map<String, Integer> freqA, Map<String, Integer> freqB) {
        Set<String> vocabulary = new HashSet<>();
        vocabulary.addAll(freqA.keySet());
        vocabulary.addAll(freqB.keySet());

        if (vocabulary.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (String term : vocabulary) {
            int a = freqA.getOrDefault(term, 0);
            int b = freqB.getOrDefault(term, 0);
            dotProduct += (double) a * b;
            magnitudeA += (double) a * a;
            magnitudeB += (double) b * b;
        }

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    /**
     * Jaccard similarity between two shingle sets: the size of the
     * intersection divided by the size of the union.
     */
    public double jaccardSimilarity(Set<String> shinglesA, Set<String> shinglesB) {
        if (shinglesA.isEmpty() && shinglesB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(shinglesA);
        intersection.retainAll(shinglesB);

        Set<String> union = new HashSet<>(shinglesA);
        union.addAll(shinglesB);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Returns the set of identical shingles (phrases) shared between both documents.
     */
    public Set<String> matchingShingles(Set<String> shinglesA, Set<String> shinglesB) {
        Set<String> intersection = new HashSet<>(shinglesA);
        intersection.retainAll(shinglesB);
        return intersection;
    }

    /**
     * Blends cosine and Jaccard similarity into one overall score.
     * Shingle (phrase-level) overlap is weighted more heavily since
     * it is the stronger signal for actual plagiarism.
     */
    public double overallSimilarity(double cosine, double jaccard) {
        double blended = (0.4 * cosine) + (0.6 * jaccard);
        return Math.round(blended * 10000.0) / 100.0; // percentage, 2 decimal places
    }
}
