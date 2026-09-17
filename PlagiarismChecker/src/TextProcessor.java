import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles all text normalization and transformation needed before
 * documents can be compared: lowercasing, punctuation stripping,
 * tokenization, stop-word removal, word-frequency maps, and
 * n-gram ("shingle") generation used for similarity detection.
 */
public class TextProcessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "so", "of", "to",
            "in", "on", "at", "for", "with", "as", "by", "is", "are", "was", "were",
            "be", "been", "being", "it", "this", "that", "these", "those", "i", "you",
            "he", "she", "they", "we", "his", "her", "their", "our", "its", "from",
            "not", "no", "do", "does", "did", "has", "have", "had", "will", "would",
            "can", "could", "should", "than", "which", "who", "whom", "what", "when",
            "where", "why", "how", "all", "any", "each", "there"
    ));

    /**
     * Lowercases text and strips out everything except letters, digits,
     * and whitespace so punctuation differences don't affect comparison.
     */
    public String normalize(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase();
        return lower.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * Splits normalized text into individual word tokens.
     */
    public String[] tokenize(String normalizedText) {
        if (normalizedText == null || normalizedText.isEmpty()) {
            return new String[0];
        }
        return normalizedText.split("\\s+");
    }

    /**
     * Removes common stop words from a token array, returning only
     * the meaningful content words.
     */
    public String[] removeStopWords(String[] tokens) {
        return Arrays.stream(tokens)
                .filter(t -> !STOP_WORDS.contains(t) && !t.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Builds a frequency map (word -> occurrence count) used for
     * cosine-similarity comparison.
     */
    public Map<String, Integer> buildFrequencyMap(String[] tokens) {
        Map<String, Integer> freq = new LinkedHashMap<>();
        for (String token : tokens) {
            freq.merge(token, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * Generates a set of n-grams ("shingles") of the given size from
     * a token array. Shingles capture word order, which is useful for
     * catching copy-pasted phrases even when isolated word frequencies
     * look different.
     *
     * @param tokens the token array (ideally after stop-word removal)
     * @param n      the shingle size, e.g. 3 for word triplets
     */
    public Set<String> generateShingles(String[] tokens, int n) {
        Set<String> shingles = new HashSet<>();
        if (tokens.length < n) {
            if (tokens.length > 0) {
                shingles.add(String.join(" ", tokens));
            }
            return shingles;
        }
        for (int i = 0; i <= tokens.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(tokens[i + j]);
            }
            shingles.add(sb.toString());
        }
        return shingles;
    }

    /**
     * Convenience pipeline: normalize -> tokenize -> remove stop words.
     */
    public String[] process(String rawText) {
        return removeStopWords(tokenize(normalize(rawText)));
    }
}
