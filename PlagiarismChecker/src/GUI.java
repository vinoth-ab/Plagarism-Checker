import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Swing-based graphical interface for the Plagiarism Checker.
 * Lets the user pick a documents folder, run a pairwise comparison
 * across every document in it, view results in a table, and export
 * a text report.
 */
public class GUI extends JFrame {

    private static final int SHINGLE_SIZE = 3;

    private final JTextField folderField;
    private final DefaultTableModel tableModel;
    private final JTable resultsTable;
    private final JLabel statusLabel;
    private final JButton exportButton;

    private final TextProcessor textProcessor = new TextProcessor();
    private final SimilarityCalculator similarityCalculator = new SimilarityCalculator();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private List<ReportGenerator.ComparisonResult> lastResults = new ArrayList<>();

    public GUI() {
        super("Plagiarism Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Top panel: folder selection ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        folderField = new JTextField("documents");
        JButton browseButton = new JButton("Browse...");
        JButton runButton = new JButton("Run Check");

        JPanel folderPanel = new JPanel(new BorderLayout(5, 5));
        folderPanel.add(new JLabel("Documents Folder:"), BorderLayout.WEST);
        folderPanel.add(folderField, BorderLayout.CENTER);
        folderPanel.add(browseButton, BorderLayout.EAST);

        topPanel.add(folderPanel, BorderLayout.CENTER);
        topPanel.add(runButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Center panel: results table ---
        String[] columns = { "Document A", "Document B", "Cosine %", "Shingle %", "Overall %", "Verdict" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom panel: status + export ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        statusLabel = new JLabel("Ready.");
        exportButton = new JButton("Export Report...");
        exportButton.setEnabled(false);

        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(exportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Actions ---
        browseButton.addActionListener(e -> chooseFolder());
        runButton.addActionListener(e -> runCheck());
        exportButton.addActionListener(e -> exportReport());
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void runCheck() {
        String folderPath = folderField.getText().trim();
        List<File> documents = FileReaderUtil.listDocuments(folderPath);

        tableModel.setRowCount(0);

        if (documents.size() < 2) {
            statusLabel.setText("Need at least 2 documents in the folder (found " + documents.size() + ").");
            exportButton.setEnabled(false);
            return;
        }

        // Pre-compute processed data for each document once.
        List<String> names = new ArrayList<>();
        List<Map<String, Integer>> freqMaps = new ArrayList<>();
        List<Set<String>> shingleSets = new ArrayList<>();

        for (File doc : documents) {
            try {
                String rawText = FileReaderUtil.readFile(doc);
                String[] tokens = textProcessor.process(rawText);
                names.add(doc.getName());
                freqMaps.add(textProcessor.buildFrequencyMap(tokens));
                shingleSets.add(textProcessor.generateShingles(tokens, SHINGLE_SIZE));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read " + doc.getName() + ": " + ex.getMessage(),
                        "File Read Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        lastResults = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                double cosine = similarityCalculator.cosineSimilarity(freqMaps.get(i), freqMaps.get(j));
                double jaccard = similarityCalculator.jaccardSimilarity(shingleSets.get(i), shingleSets.get(j));
                double overall = similarityCalculator.overallSimilarity(cosine, jaccard);

                ReportGenerator.ComparisonResult result =
                        new ReportGenerator.ComparisonResult(names.get(i), names.get(j), cosine, jaccard, overall);
                lastResults.add(result);

                tableModel.addRow(new Object[] {
                        result.documentA,
                        result.documentB,
                        String.format("%.2f", cosine * 100),
                        String.format("%.2f", jaccard * 100),
                        String.format("%.2f", overall),
                        result.verdict()
                });
            }
        }

        statusLabel.setText("Compared " + names.size() + " documents (" + lastResults.size() + " pairs).");
        exportButton.setEnabled(true);
    }

    private void exportReport() {
        if (lastResults.isEmpty()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("plagiarism_report.txt"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String report = reportGenerator.buildReport(lastResults);
        try {
            reportGenerator.exportToFile(report, chooser.getSelectedFile().getAbsolutePath());
            statusLabel.setText("Report exported to " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
