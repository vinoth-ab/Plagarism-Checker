import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds a human-readable plagiarism report from a list of pairwise
 * comparison results and can export it to a text file.
 */
public class ReportGenerator {

    /** Simple data holder for one document-pair comparison result. */
    public static class ComparisonResult {
        public final String documentA;
        public final String documentB;
        public final double cosineScore;
        public final double jaccardScore;
        public final double overallScore;

        public ComparisonResult(String documentA, String documentB,
                                 double cosineScore, double jaccardScore, double overallScore) {
            this.documentA = documentA;
            this.documentB = documentB;
            this.cosineScore = cosineScore;
            this.jaccardScore = jaccardScore;
            this.overallScore = overallScore;
        }

        public String verdict() {
            if (overallScore >= 70) return "HIGH SIMILARITY - Likely Plagiarism";
            if (overallScore >= 40) return "MODERATE SIMILARITY - Review Recommended";
            return "LOW SIMILARITY";
        }
    }

    /**
     * Builds a formatted plain-text report from the comparison results.
     */
    public String buildReport(List<ComparisonResult> results) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("=================================================\n");
        sb.append("            PLAGIARISM CHECK REPORT\n");
        sb.append("=================================================\n");
        sb.append("Generated: ").append(timestamp).append("\n");
        sb.append("Total comparisons: ").append(results.size()).append("\n");
        sb.append("-------------------------------------------------\n\n");

        if (results.isEmpty()) {
            sb.append("No documents were compared. Add at least two documents\n");
            sb.append("to the 'documents' folder and run the check again.\n");
            return sb.toString();
        }

        int index = 1;
        for (ComparisonResult r : results) {
            sb.append(String.format("[%d] %s  vs  %s%n", index++, r.documentA, r.documentB));
            sb.append(String.format("    Cosine Similarity  : %.2f%%%n", r.cosineScore * 100));
            sb.append(String.format("    Shingle Similarity : %.2f%%%n", r.jaccardScore * 100));
            sb.append(String.format("    Overall Score      : %.2f%%%n", r.overallScore));
            sb.append(String.format("    Verdict            : %s%n", r.verdict()));
            sb.append('\n');
        }

        sb.append("-------------------------------------------------\n");
        sb.append("Scoring guide: 0-39% Low | 40-69% Moderate | 70-100% High\n");
        sb.append("=================================================\n");

        return sb.toString();
    }

    /**
     * Writes the given report text to a file on disk.
     *
     * @param report   the report text (from buildReport)
     * @param filePath destination file path, e.g. "report.txt"
     */
    public void exportToFile(String report, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(report);
        }
    }
}
