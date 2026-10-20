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
     * Builds a modern, styled HTML plagiarism report.
     */
    public String buildHtmlReport(List<ComparisonResult> results) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long flaggedCount = results.stream().filter(r -> r.overallScore >= 70).count();
        long moderateCount = results.stream().filter(r -> r.overallScore >= 40 && r.overallScore < 70).count();

        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n")
          .append("<title>Plagiarism Check Report</title>\n")
          .append("<style>\n")
          .append("  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8fafc; color: #1e293b; margin: 0; padding: 30px; }\n")
          .append("  .container { max-width: 900px; margin: 0 auto; background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); overflow: hidden; }\n")
          .append("  .header { background: linear-gradient(135deg, #1e293b, #0f172a); color: white; padding: 28px; }\n")
          .append("  .header h1 { margin: 0 0 6px 0; font-size: 24px; }\n")
          .append("  .header p { margin: 0; color: #94a3b8; font-size: 14px; }\n")
          .append("  .stats { display: flex; gap: 16px; padding: 20px 28px; background: #f1f5f9; border-bottom: 1px solid #e2e8f0; }\n")
          .append("  .stat-card { flex: 1; background: white; padding: 14px; border-radius: 8px; border: 1px solid #cbd5e1; }\n")
          .append("  .stat-label { font-size: 12px; color: #64748b; text-transform: uppercase; font-weight: 600; }\n")
          .append("  .stat-val { font-size: 22px; font-weight: bold; margin-top: 4px; }\n")
          .append("  .table-container { padding: 24px 28px; }\n")
          .append("  table { width: 100%; border-collapse: collapse; font-size: 14px; }\n")
          .append("  th { text-align: left; padding: 12px; background: #f8fafc; border-bottom: 2px solid #e2e8f0; color: #475569; }\n")
          .append("  td { padding: 12px; border-bottom: 1px solid #e2e8f0; vertical-align: middle; }\n")
          .append("  tr:hover { background: #f8fafc; }\n")
          .append("  .badge { display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; }\n")
          .append("  .badge-high { background: #fee2e2; color: #b91c1c; border: 1px solid #fca5a5; }\n")
          .append("  .badge-mod { background: #fef3c7; color: #b45309; border: 1px solid #fcd34d; }\n")
          .append("  .badge-low { background: #dcfce7; color: #15803d; border: 1px solid #86efac; }\n")
          .append("  .progress-bg { background: #e2e8f0; border-radius: 9999px; height: 8px; width: 80px; overflow: hidden; display: inline-block; vertical-align: middle; margin-right: 8px; }\n")
          .append("  .progress-bar { height: 100%; border-radius: 9999px; }\n")
          .append("  .footer { padding: 16px 28px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b; text-align: center; }\n")
          .append("</style>\n</head>\n<body>\n")
          .append("<div class=\"container\">\n")
          .append("  <div class=\"header\">\n")
          .append("    <h1>🔍 Plagiarism Analysis Report</h1>\n")
          .append("    <p>Generated on ").append(timestamp).append("</p>\n")
          .append("  </div>\n")
          .append("  <div class=\"stats\">\n")
          .append("    <div class=\"stat-card\"><div class=\"stat-label\">Comparisons</div><div class=\"stat-val\">").append(results.size()).append("</div></div>\n")
          .append("    <div class=\"stat-card\"><div class=\"stat-label\">High Risk (≥70%)</div><div class=\"stat-val\" style=\"color:#b91c1c;\">").append(flaggedCount).append("</div></div>\n")
          .append("    <div class=\"stat-card\"><div class=\"stat-label\">Moderate (40-69%)</div><div class=\"stat-val\" style=\"color:#b45309;\">").append(moderateCount).append("</div></div>\n")
          .append("  </div>\n")
          .append("  <div class=\"table-container\">\n")
          .append("    <table>\n")
          .append("      <thead><tr><th>#</th><th>Document A</th><th>Document B</th><th>Vocabulary %</th><th>Phrase %</th><th>Overall Score</th><th>Verdict</th></tr></thead>\n")
          .append("      <tbody>\n");

        int idx = 1;
        for (ComparisonResult r : results) {
            String badgeClass = r.overallScore >= 70 ? "badge-high" : (r.overallScore >= 40 ? "badge-mod" : "badge-low");
            String color = r.overallScore >= 70 ? "#ef4444" : (r.overallScore >= 40 ? "#f59e0b" : "#10b981");
            sb.append("      <tr>\n")
              .append("        <td>").append(idx++).append("</td>\n")
              .append("        <td><strong>").append(r.documentA).append("</strong></td>\n")
              .append("        <td><strong>").append(r.documentB).append("</strong></td>\n")
              .append(String.format("        <td>%.1f%%</td>%n", r.cosineScore * 100))
              .append(String.format("        <td>%.1f%%</td>%n", r.jaccardScore * 100))
              .append(String.format("        <td><span class=\"progress-bg\"><span class=\"progress-bar\" style=\"width:%.1f%%; background:%s;\"></span></span><strong>%.2f%%</strong></td>%n", r.overallScore, color, r.overallScore))
              .append("        <td><span class=\"badge ").append(badgeClass).append("\">").append(r.verdict()).append("</span></td>\n")
              .append("      </tr>\n");
        }

        sb.append("      </tbody>\n")
          .append("    </table>\n")
          .append("  </div>\n")
          .append("  <div class=\"footer\">Plagiarism Checker Report &bull; Cosine Vocabulary (40%) + Shingle N-Gram Phrase Overlap (60%)</div>\n")
          .append("</div>\n</body>\n</html>\n");

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
