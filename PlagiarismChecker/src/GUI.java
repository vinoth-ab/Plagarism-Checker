import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern, easy-to-use GUI for the Plagiarism Checker.
 * Features:
 * - Direct Side-by-Side Comparison of 2 texts/files with match highlighting
 * - Batch Directory Comparison with summary stat cards, search filter, and visual progress bars
 * - Drag-and-Drop file and folder support
 * - HTML and Plain-Text report exports
 * - Settings and algorithm explanation guide
 */
public class GUI extends JFrame {

    private int shingleSize = 3;
    private int highThreshold = 70;
    private int moderateThreshold = 40;

    // Backend components
    private final TextProcessor textProcessor = new TextProcessor();
    private final SimilarityCalculator similarityCalculator = new SimilarityCalculator();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    // Tabbed layout
    private JTabbedPane tabbedPane;

    // Tab 1: Direct comparison controls
    private JTextArea textAreaA;
    private JTextArea textAreaB;
    private JLabel labelStatsA;
    private JLabel labelStatsB;
    private JLabel labelDocAName;
    private JLabel labelDocBName;
    private JPanel directResultsPanel;
    private JLabel labelOverallScore;
    private JLabel labelDirectVerdict;
    private JLabel labelCosineScore;
    private JLabel labelShingleScore;
    private DefaultListModel<String> matchingPhrasesListModel;
    private JList<String> matchingPhrasesList;
    private JCheckBox cbHighlightMatches;

    // Tab 2: Batch comparison controls
    private JTextField folderField;
    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private JLabel statDocsCount;
    private JLabel statPairsCount;
    private JLabel statMaxScore;
    private JLabel statFlaggedCount;
    private JButton exportTextButton;
    private JButton exportHtmlButton;
    private JButton inspectPairButton;

    // Status bar
    private JLabel statusLabel;

    // Data cache
    private List<ReportGenerator.ComparisonResult> lastResults = new ArrayList<>();
    private final Map<String, File> scannedFilesMap = new HashMap<>();

    public GUI() {
        super("Plagiarism Checker Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(840, 560));
        setLocationRelativeTo(null);

        // Enable system antialiasing for crisp text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(UITheme.BG_APP);

        // 1. Top Header Banner
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.BG_APP);

        tabbedPane.addTab("  📄 Side-by-Side Comparison  ", createDirectComparisonTab());
        tabbedPane.addTab("  📁 Folder Batch Scanner  ", createBatchComparisonTab());
        tabbedPane.addTab("  ⚙️ Settings & Guide  ", createSettingsTab());

        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        mainContainer.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainContainer);

        // Pre-fill with sample texts if available
        loadSampleComparison();
    }

    // ==========================================
    // UI SECTION: Header
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Slate 900
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🛡️ Plagiarism Checker");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Instant pairwise text comparison, n-gram shingle matching & folder scanner");
        subLabel.setFont(UITheme.FONT_REGULAR);
        subLabel.setForeground(new Color(148, 163, 184)); // Slate 400

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);

        JButton loadSamplesBtn = UITheme.createSecondaryButton("📂 Load Sample Files");
        loadSamplesBtn.addActionListener(e -> loadSampleComparison());
        quickActions.add(loadSamplesBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(quickActions, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // UI SECTION: Tab 1 - Side-by-Side Comparison
    // ==========================================
    private JPanel createDirectComparisonTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Center split for Document A and Document B
        JPanel documentsGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        documentsGrid.setOpaque(false);

        // Document A Box
        JPanel panelA = UITheme.createCard();
        panelA.setLayout(new BorderLayout(5, 5));
        JPanel topA = new JPanel(new BorderLayout(5, 0));
        topA.setOpaque(false);
        labelDocAName = new JLabel("Document A (Original / Source)");
        labelDocAName.setFont(UITheme.FONT_HEADER);
        labelDocAName.setForeground(UITheme.TEXT_DARK);

        JPanel actionsA = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionsA.setOpaque(false);
        JButton btnOpenA = UITheme.createSecondaryButton("Open File...");
        JButton btnClearA = UITheme.createSecondaryButton("Clear");
        actionsA.add(btnOpenA);
        actionsA.add(btnClearA);
        topA.add(labelDocAName, BorderLayout.WEST);
        topA.add(actionsA, BorderLayout.EAST);

        textAreaA = new JTextArea();
        textAreaA.setFont(UITheme.FONT_MONO);
        textAreaA.setLineWrap(true);
        textAreaA.setWrapStyleWord(true);
        textAreaA.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane scrollA = new JScrollPane(textAreaA);
        scrollA.setBorder(new LineBorder(UITheme.BORDER_LIGHT, 1));

        labelStatsA = new JLabel("Words: 0 | Chars: 0");
        labelStatsA.setFont(UITheme.FONT_SMALL);
        labelStatsA.setForeground(UITheme.TEXT_MUTED);

        panelA.add(topA, BorderLayout.NORTH);
        panelA.add(scrollA, BorderLayout.CENTER);
        panelA.add(labelStatsA, BorderLayout.SOUTH);

        // Document B Box
        JPanel panelB = UITheme.createCard();
        panelB.setLayout(new BorderLayout(5, 5));
        JPanel topB = new JPanel(new BorderLayout(5, 0));
        topB.setOpaque(false);
        labelDocBName = new JLabel("Document B (Suspect / Comparison)");
        labelDocBName.setFont(UITheme.FONT_HEADER);
        labelDocBName.setForeground(UITheme.TEXT_DARK);

        JPanel actionsB = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionsB.setOpaque(false);
        JButton btnOpenB = UITheme.createSecondaryButton("Open File...");
        JButton btnClearB = UITheme.createSecondaryButton("Clear");
        actionsB.add(btnOpenB);
        actionsB.add(btnClearB);
        topB.add(labelDocBName, BorderLayout.WEST);
        topB.add(actionsB, BorderLayout.EAST);

        textAreaB = new JTextArea();
        textAreaB.setFont(UITheme.FONT_MONO);
        textAreaB.setLineWrap(true);
        textAreaB.setWrapStyleWord(true);
        textAreaB.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane scrollB = new JScrollPane(textAreaB);
        scrollB.setBorder(new LineBorder(UITheme.BORDER_LIGHT, 1));

        labelStatsB = new JLabel("Words: 0 | Chars: 0");
        labelStatsB.setFont(UITheme.FONT_SMALL);
        labelStatsB.setForeground(UITheme.TEXT_MUTED);

        panelB.add(topB, BorderLayout.NORTH);
        panelB.add(scrollB, BorderLayout.CENTER);
        panelB.add(labelStatsB, BorderLayout.SOUTH);

        documentsGrid.add(panelA);
        documentsGrid.add(panelB);

        // Action Toolbar between inputs and results
        JPanel middleBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        middleBar.setOpaque(false);

        JButton compareBtn = UITheme.createPrimaryButton("⚡ Compare Documents");
        compareBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton swapBtn = UITheme.createSecondaryButton("⇄ Swap A & B");
        cbHighlightMatches = new JCheckBox("Highlight matching phrases in text", true);
        cbHighlightMatches.setFont(UITheme.FONT_REGULAR);
        cbHighlightMatches.setOpaque(false);

        middleBar.add(compareBtn);
        middleBar.add(swapBtn);
        middleBar.add(cbHighlightMatches);

        // Results Card (Bottom)
        directResultsPanel = UITheme.createCard();
        directResultsPanel.setLayout(new BorderLayout(12, 8));

        JPanel scoreHeader = new JPanel(new BorderLayout());
        scoreHeader.setOpaque(false);

        JPanel leftScore = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftScore.setOpaque(false);

        labelOverallScore = new JLabel("Similarity: --");
        labelOverallScore.setFont(new Font("Segoe UI", Font.BOLD, 22));
        labelOverallScore.setForeground(UITheme.TEXT_DARK);

        labelDirectVerdict = new JLabel("Ready to compare");
        labelDirectVerdict.setFont(UITheme.FONT_BOLD);
        labelDirectVerdict.setOpaque(true);
        labelDirectVerdict.setBackground(UITheme.SECONDARY_BG);
        labelDirectVerdict.setBorder(new EmptyBorder(4, 10, 4, 10));

        leftScore.add(labelOverallScore);
        leftScore.add(labelDirectVerdict);

        JPanel rightMetrics = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightMetrics.setOpaque(false);
        labelCosineScore = new JLabel("Vocabulary Match: --");
        labelCosineScore.setFont(UITheme.FONT_REGULAR);
        labelShingleScore = new JLabel("Phrase Overlap: --");
        labelShingleScore.setFont(UITheme.FONT_REGULAR);
        rightMetrics.add(labelCosineScore);
        rightMetrics.add(labelShingleScore);

        scoreHeader.add(leftScore, BorderLayout.WEST);
        scoreHeader.add(rightMetrics, BorderLayout.EAST);

        // Matching phrases list
        matchingPhrasesListModel = new DefaultListModel<>();
        matchingPhrasesList = new JList<>(matchingPhrasesListModel);
        matchingPhrasesList.setFont(UITheme.FONT_MONO);
        matchingPhrasesList.setVisibleRowCount(3);
        JScrollPane phrasesScroll = new JScrollPane(matchingPhrasesList);
        phrasesScroll.setPreferredSize(new Dimension(800, 75));
        phrasesScroll.setBorder(BorderFactory.createTitledBorder("Matching N-Gram Phrases (Click phrase to locate in text)"));

        directResultsPanel.add(scoreHeader, BorderLayout.NORTH);
        directResultsPanel.add(phrasesScroll, BorderLayout.CENTER);

        // Assemble Tab 1
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 8));
        centerWrapper.setOpaque(false);
        centerWrapper.add(documentsGrid, BorderLayout.CENTER);
        centerWrapper.add(middleBar, BorderLayout.SOUTH);

        panel.add(centerWrapper, BorderLayout.CENTER);
        panel.add(directResultsPanel, BorderLayout.SOUTH);

        // Drag and Drop listeners
        UITheme.setupDragAndDrop(textAreaA, files -> {
            if (!files.isEmpty()) loadFileIntoDocument(files.get(0), true);
        });
        UITheme.setupDragAndDrop(textAreaB, files -> {
            if (!files.isEmpty()) loadFileIntoDocument(files.get(0), false);
        });

        // Event Listeners
        btnOpenA.addActionListener(e -> chooseSingleFile(true));
        btnOpenB.addActionListener(e -> chooseSingleFile(false));
        btnClearA.addActionListener(e -> { textAreaA.setText(""); updateDocStats(true); });
        btnClearB.addActionListener(e -> { textAreaB.setText(""); updateDocStats(false); });

        swapBtn.addActionListener(e -> {
            String tempText = textAreaA.getText();
            String tempName = labelDocAName.getText();
            textAreaA.setText(textAreaB.getText());
            labelDocAName.setText(labelDocBName.getText());
            textAreaB.setText(tempText);
            labelDocBName.setText(tempName);
            updateDocStats(true);
            updateDocStats(false);
            runDirectComparison();
        });

        compareBtn.addActionListener(e -> runDirectComparison());

        textAreaA.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateDocStats(true); }
            public void removeUpdate(DocumentEvent e) { updateDocStats(true); }
            public void changedUpdate(DocumentEvent e) { updateDocStats(true); }
        });
        textAreaB.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateDocStats(false); }
            public void removeUpdate(DocumentEvent e) { updateDocStats(false); }
            public void changedUpdate(DocumentEvent e) { updateDocStats(false); }
        });

        matchingPhrasesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = matchingPhrasesList.getSelectedValue();
                if (selected != null) {
                    highlightSpecificPhrase(selected);
                }
            }
        });

        return panel;
    }

    // ==========================================
    // UI SECTION: Tab 2 - Folder Batch Scanner
    // ==========================================
    private JPanel createBatchComparisonTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Folder Controls Card
        JPanel topFolderCard = UITheme.createCard();
        topFolderCard.setLayout(new BorderLayout(10, 8));

        JPanel folderInputPanel = new JPanel(new BorderLayout(8, 0));
        folderInputPanel.setOpaque(false);

        JLabel folderIcon = new JLabel("Folder: ");
        folderIcon.setFont(UITheme.FONT_BOLD);
        folderField = new JTextField("documents");
        folderField.setFont(UITheme.FONT_REGULAR);
        folderField.setPreferredSize(new Dimension(300, 32));

        JPanel folderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        folderButtons.setOpaque(false);

        JButton browseBtn = UITheme.createSecondaryButton("Browse...");
        JButton useDefaultBtn = UITheme.createSecondaryButton("Use 'documents/'");
        JButton runScanBtn = UITheme.createPrimaryButton("⚡ Scan Folder");

        folderButtons.add(browseBtn);
        folderButtons.add(useDefaultBtn);
        folderButtons.add(runScanBtn);

        folderInputPanel.add(folderIcon, BorderLayout.WEST);
        folderInputPanel.add(folderField, BorderLayout.CENTER);
        folderInputPanel.add(folderButtons, BorderLayout.EAST);

        JLabel hintLabel = new JLabel("💡 Tip: Drag and drop any folder or multiple text files directly into this window to scan.");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        topFolderCard.add(folderInputPanel, BorderLayout.CENTER);
        topFolderCard.add(hintLabel, BorderLayout.SOUTH);

        // 2. Metrics Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);

        statDocsCount = new JLabel("0");
        statPairsCount = new JLabel("0");
        statMaxScore = new JLabel("0.0%");
        statFlaggedCount = new JLabel("0");

        statsRow.add(createStatCard("DOCUMENTS SCANNED", statDocsCount, "Plain text / code files", UITheme.PRIMARY));
        statsRow.add(createStatCard("PAIRS COMPARED", statPairsCount, "Pairwise combinations", UITheme.TEXT_DARK));
        statsRow.add(createStatCard("HIGHEST SIMILARITY", statMaxScore, "Peak matched pair", UITheme.MOD_FG));
        statsRow.add(createStatCard("PLAGIARISM FLAGGED", statFlaggedCount, "Scores ≥ 70%", UITheme.HIGH_FG));

        // 3. Filter Bar & Table Actions
        JPanel filterBar = new JPanel(new BorderLayout(8, 0));
        filterBar.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilter.setOpaque(false);
        leftFilter.add(new JLabel("Search:"));
        searchField = new JTextField(14);
        searchField.setFont(UITheme.FONT_REGULAR);
        leftFilter.add(searchField);

        leftFilter.add(new JLabel("Verdict:"));
        filterComboBox = new JComboBox<>(new String[]{"All Verdicts", "High Similarity (≥70%)", "Moderate (40-69%)", "Low (<40%)"});
        filterComboBox.setFont(UITheme.FONT_REGULAR);
        leftFilter.add(filterComboBox);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightActions.setOpaque(false);
        inspectPairButton = UITheme.createSecondaryButton("🔍 Inspect in Side-by-Side");
        inspectPairButton.setEnabled(false);
        exportTextButton = UITheme.createSecondaryButton("📄 Export TXT");
        exportTextButton.setEnabled(false);
        exportHtmlButton = UITheme.createSecondaryButton("🌐 Export HTML");
        exportHtmlButton.setEnabled(false);

        rightActions.add(inspectPairButton);
        rightActions.add(exportTextButton);
        rightActions.add(exportHtmlButton);

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(rightActions, BorderLayout.EAST);

        // 4. Results Table Card
        String[] columns = {"Document A", "Document B", "Vocabulary %", "Phrase %", "Overall Score", "Verdict"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class;
                return String.class;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(28);
        resultsTable.setFont(UITheme.FONT_REGULAR);
        resultsTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        resultsTable.getTableHeader().setBackground(UITheme.SECONDARY_BG);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setGridColor(UITheme.BORDER_LIGHT);

        rowSorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(rowSorter);

        // Apply custom renderers
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new UITheme.SimilarityProgressRenderer());
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.VerdictBadgeRenderer());

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(new LineBorder(UITheme.BORDER_LIGHT, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(statsRow, BorderLayout.NORTH);
        centerPanel.add(filterBar, BorderLayout.CENTER);

        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 6));
        tableCard.add(centerPanel, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        panel.add(topFolderCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        // Drag & Drop for folder scanner
        UITheme.setupDragAndDrop(panel, files -> {
            if (!files.isEmpty()) {
                File first = files.get(0);
                if (first.isDirectory()) {
                    folderField.setText(first.getAbsolutePath());
                    runBatchCheck();
                } else if (first.getParentFile() != null) {
                    folderField.setText(first.getParentFile().getAbsolutePath());
                    runBatchCheck();
                }
            }
        });

        // Event listeners
        browseBtn.addActionListener(e -> chooseFolder());
        useDefaultBtn.addActionListener(e -> {
            folderField.setText("documents");
            runBatchCheck();
        });
        runScanBtn.addActionListener(e -> runBatchCheck());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyTableFilters(); }
            public void removeUpdate(DocumentEvent e) { applyTableFilters(); }
            public void changedUpdate(DocumentEvent e) { applyTableFilters(); }
        });
        filterComboBox.addActionListener(e -> applyTableFilters());

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = resultsTable.getSelectedRow() != -1;
            inspectPairButton.setEnabled(hasSelection);
        });

        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && resultsTable.getSelectedRow() != -1) {
                    inspectSelectedPair();
                }
            }
        });

        inspectPairButton.addActionListener(e -> inspectSelectedPair());
        exportTextButton.addActionListener(e -> exportReport(false));
        exportHtmlButton.addActionListener(e -> exportReport(true));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color color) {
        JPanel card = UITheme.createCard();
        card.setLayout(new GridLayout(3, 1, 0, 2));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(titleLbl);
        card.add(valueLabel);
        card.add(subLbl);
        return card;
    }

    // ==========================================
    // UI SECTION: Tab 3 - Settings & Guide
    // ==========================================
    private JPanel createSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Parameters Card
        JPanel paramsCard = UITheme.createCard();
        paramsCard.setLayout(new GridLayout(3, 2, 16, 12));
        paramsCard.setMaximumSize(new Dimension(800, 140));

        JLabel shingleLbl = new JLabel("Shingle Size (N-gram word sequence length):");
        shingleLbl.setFont(UITheme.FONT_BOLD);
        JSpinner shingleSpinner = new JSpinner(new SpinnerNumberModel(shingleSize, 2, 6, 1));
        shingleSpinner.addChangeListener(e -> shingleSize = (Integer) shingleSpinner.getValue());

        JLabel highLbl = new JLabel("High Similarity Threshold (%):");
        highLbl.setFont(UITheme.FONT_BOLD);
        JSpinner highSpinner = new JSpinner(new SpinnerNumberModel(highThreshold, 50, 95, 5));
        highSpinner.addChangeListener(e -> highThreshold = (Integer) highSpinner.getValue());

        JLabel modLbl = new JLabel("Moderate Similarity Threshold (%):");
        modLbl.setFont(UITheme.FONT_BOLD);
        JSpinner modSpinner = new JSpinner(new SpinnerNumberModel(moderateThreshold, 20, 60, 5));
        modSpinner.addChangeListener(e -> moderateThreshold = (Integer) modSpinner.getValue());

        paramsCard.add(shingleLbl);
        paramsCard.add(shingleSpinner);
        paramsCard.add(highLbl);
        paramsCard.add(highSpinner);
        paramsCard.add(modLbl);
        paramsCard.add(modSpinner);

        // Guide Card
        JPanel guideCard = UITheme.createCard();
        guideCard.setLayout(new BorderLayout(8, 8));

        JEditorPane guidePane = new JEditorPane();
        guidePane.setContentType("text/html");
        guidePane.setEditable(false);
        guidePane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 11px; color: #1e293b; padding: 4px;'>" +
                "<h3 style='margin-top:0; color: #4f46e5;'>How Plagiarism Detection Works</h3>" +
                "<p><strong>1. Cosine Similarity (Vocabulary Match - 40% Weight):</strong><br>" +
                "Converts the document words into normalized frequency vectors after stripping stop words. Measures if both documents discuss the same subject or share the same vocabulary distribution.</p>" +
                "<p><strong>2. Jaccard Shingle Overlap (Phrase Copying - 60% Weight):</strong><br>" +
                "Breaks the text into sequences of N consecutive words (shingles). Because it preserves word ordering, it is a powerful indicator of directly copy-pasted or lightly paraphrased sentences.</p>" +
                "<p><strong>Supported File Formats:</strong><br>" +
                "<code>" + String.join(", ", FileReaderUtil.getSupportedExtensions()) + "</code></p>" +
                "</body></html>");

        guideCard.add(guidePane, BorderLayout.CENTER);

        content.add(paramsCard);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(guideCard);

        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    // ==========================================
    // UI SECTION: Bottom Status Bar
    // ==========================================
    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_LIGHT, 1, false),
                new EmptyBorder(6, 14, 6, 14)
        ));

        statusLabel = new JLabel("Ready. Choose two documents or select a folder to start.");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel versionLabel = new JLabel("Plagiarism Checker Pro v2.0");
        versionLabel.setFont(UITheme.FONT_SMALL);
        versionLabel.setForeground(UITheme.TEXT_MUTED);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);
        return statusPanel;
    }

    // ==========================================
    // BUSINESS LOGIC: Direct Comparison
    // ==========================================
    private void runDirectComparison() {
        String textA = textAreaA.getText();
        String textB = textAreaB.getText();

        if (textA.trim().isEmpty() || textB.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please provide text or load files for both Document A and Document B.",
                    "Input Needed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Process texts
        String[] tokensA = textProcessor.process(textA);
        String[] tokensB = textProcessor.process(textB);

        Map<String, Integer> freqA = textProcessor.buildFrequencyMap(tokensA);
        Map<String, Integer> freqB = textProcessor.buildFrequencyMap(tokensB);

        Set<String> shinglesA = textProcessor.generateShingles(tokensA, shingleSize);
        Set<String> shinglesB = textProcessor.generateShingles(tokensB, shingleSize);

        double cosine = similarityCalculator.cosineSimilarity(freqA, freqB);
        double jaccard = similarityCalculator.jaccardSimilarity(shinglesA, shinglesB);
        double overall = similarityCalculator.overallSimilarity(cosine, jaccard);

        Set<String> commonShingles = similarityCalculator.matchingShingles(shinglesA, shinglesB);

        // Update score displays
        labelOverallScore.setText(String.format("Similarity: %.2f%%", overall));
        labelCosineScore.setText(String.format("Vocabulary Match: %.1f%%", cosine * 100));
        labelShingleScore.setText(String.format("Phrase Overlap: %.1f%%", jaccard * 100));

        if (overall >= highThreshold) {
            labelDirectVerdict.setText("HIGH SIMILARITY - Likely Plagiarism");
            labelDirectVerdict.setBackground(UITheme.HIGH_BG);
            labelDirectVerdict.setForeground(UITheme.HIGH_FG);
            labelOverallScore.setForeground(UITheme.HIGH_FG);
        } else if (overall >= moderateThreshold) {
            labelDirectVerdict.setText("MODERATE SIMILARITY - Review Recommended");
            labelDirectVerdict.setBackground(UITheme.MOD_BG);
            labelDirectVerdict.setForeground(UITheme.MOD_FG);
            labelOverallScore.setForeground(UITheme.MOD_FG);
        } else {
            labelDirectVerdict.setText("LOW SIMILARITY - Unlikely Plagiarism");
            labelDirectVerdict.setBackground(UITheme.LOW_BG);
            labelDirectVerdict.setForeground(UITheme.LOW_FG);
            labelOverallScore.setForeground(UITheme.LOW_FG);
        }

        // Populate matching phrases
        matchingPhrasesListModel.clear();
        for (String s : commonShingles) {
            matchingPhrasesListModel.addElement(s);
        }

        // Highlight matching phrases if enabled
        if (cbHighlightMatches.isSelected()) {
            highlightAllMatchingShingles(commonShingles);
        } else {
            clearHighlights();
        }

        statusLabel.setText(String.format("Comparison complete: %.1f%% similarity (%d matching phrases detected).",
                overall, commonShingles.size()));
    }

    private void highlightAllMatchingShingles(Set<String> shingles) {
        clearHighlights();
        Highlighter hlA = textAreaA.getHighlighter();
        Highlighter hlB = textAreaB.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(254, 240, 138, 160));

        String textA = textAreaA.getText().toLowerCase();
        String textB = textAreaB.getText().toLowerCase();

        for (String shingle : shingles) {
            highlightOccurrences(textA, shingle, hlA, painter);
            highlightOccurrences(textB, shingle, hlB, painter);
        }
    }

    private void highlightSpecificPhrase(String phrase) {
        clearHighlights();
        Highlighter hlA = textAreaA.getHighlighter();
        Highlighter hlB = textAreaB.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(253, 224, 71));

        String textA = textAreaA.getText().toLowerCase();
        String textB = textAreaB.getText().toLowerCase();

        highlightOccurrences(textA, phrase.toLowerCase(), hlA, painter);
        highlightOccurrences(textB, phrase.toLowerCase(), hlB, painter);
    }

    private void highlightOccurrences(String fullText, String search, Highlighter highlighter, Highlighter.HighlightPainter painter) {
        if (search.isEmpty()) return;
        int idx = 0;
        while ((idx = fullText.indexOf(search, idx)) >= 0) {
            try {
                highlighter.addHighlight(idx, idx + search.length(), painter);
            } catch (Exception ignored) {}
            idx += search.length();
        }
    }

    private void clearHighlights() {
        textAreaA.getHighlighter().removeAllHighlights();
        textAreaB.getHighlighter().removeAllHighlights();
    }

    private void updateDocStats(boolean isDocA) {
        JTextArea area = isDocA ? textAreaA : textAreaB;
        JLabel statsLbl = isDocA ? labelStatsA : labelStatsB;

        String text = area.getText();
        int chars = text.length();
        int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;

        statsLbl.setText("Words: " + words + " | Chars: " + chars);
    }

    private void chooseSingleFile(boolean isDocA) {
        JFileChooser chooser = new JFileChooser();
        File defaultDocs = new File("documents");
        if (defaultDocs.exists()) {
            chooser.setCurrentDirectory(defaultDocs);
        }
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            loadFileIntoDocument(chooser.getSelectedFile(), isDocA);
        }
    }

    private void loadFileIntoDocument(File file, boolean isDocA) {
        try {
            String content = FileReaderUtil.readFile(file);
            if (isDocA) {
                textAreaA.setText(content);
                labelDocAName.setText("Document A: " + file.getName());
                updateDocStats(true);
            } else {
                textAreaB.setText(content);
                labelDocBName.setText("Document B: " + file.getName());
                updateDocStats(false);
            }
            statusLabel.setText("Loaded " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage(),
                    "Read Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSampleComparison() {
        File sample1 = new File("documents/sample1.txt");
        File sample2 = new File("documents/sample2.txt");

        if (sample1.exists() && sample2.exists()) {
            loadFileIntoDocument(sample1, true);
            loadFileIntoDocument(sample2, false);
            runDirectComparison();
        }
    }

    // ==========================================
    // BUSINESS LOGIC: Batch Folder Check
    // ==========================================
    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            runBatchCheck();
        }
    }

    private void runBatchCheck() {
        String folderPath = folderField.getText().trim();
        List<File> documents = FileReaderUtil.listDocuments(folderPath);

        tableModel.setRowCount(0);
        lastResults.clear();
        scannedFilesMap.clear();

        if (documents.size() < 2) {
            statDocsCount.setText(String.valueOf(documents.size()));
            statPairsCount.setText("0");
            statMaxScore.setText("0.0%");
            statFlaggedCount.setText("0");
            exportTextButton.setEnabled(false);
            exportHtmlButton.setEnabled(false);
            inspectPairButton.setEnabled(false);
            statusLabel.setText("Found " + documents.size() + " supported files in folder. Need at least 2 documents.");
            JOptionPane.showMessageDialog(this,
                    "Need at least 2 readable documents in '" + folderPath + "'.\nSupported extensions: " +
                            String.join(", ", FileReaderUtil.getSupportedExtensions()),
                    "Insufficient Documents", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (File doc : documents) {
            scannedFilesMap.put(doc.getName(), doc);
        }

        // Precompute processed tokens and shingles
        List<String> names = new ArrayList<>();
        List<Map<String, Integer>> freqMaps = new ArrayList<>();
        List<Set<String>> shingleSets = new ArrayList<>();

        for (File doc : documents) {
            try {
                String rawText = FileReaderUtil.readFile(doc);
                String[] tokens = textProcessor.process(rawText);
                names.add(doc.getName());
                freqMaps.add(textProcessor.buildFrequencyMap(tokens));
                shingleSets.add(textProcessor.generateShingles(tokens, shingleSize));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read " + doc.getName() + ": " + ex.getMessage(),
                        "File Read Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        double maxScore = 0.0;
        int flaggedCount = 0;

        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                double cosine = similarityCalculator.cosineSimilarity(freqMaps.get(i), freqMaps.get(j));
                double jaccard = similarityCalculator.jaccardSimilarity(shingleSets.get(i), shingleSets.get(j));
                double overall = similarityCalculator.overallSimilarity(cosine, jaccard);

                if (overall > maxScore) maxScore = overall;
                if (overall >= highThreshold) flaggedCount++;

                ReportGenerator.ComparisonResult res =
                        new ReportGenerator.ComparisonResult(names.get(i), names.get(j), cosine, jaccard, overall);
                lastResults.add(res);

                tableModel.addRow(new Object[]{
                        res.documentA,
                        res.documentB,
                        String.format("%.1f%%", cosine * 100),
                        String.format("%.1f%%", jaccard * 100),
                        overall,
                        res.verdict()
                });
            }
        }

        statDocsCount.setText(String.valueOf(documents.size()));
        statPairsCount.setText(String.valueOf(lastResults.size()));
        statMaxScore.setText(String.format("%.1f%%", maxScore));
        statFlaggedCount.setText(String.valueOf(flaggedCount));

        exportTextButton.setEnabled(true);
        exportHtmlButton.setEnabled(true);
        statusLabel.setText("Scanned " + documents.size() + " files (" + lastResults.size() + " pairs analyzed).");
    }

    private void applyTableFilters() {
        String query = searchField.getText().trim();
        String verdictFilter = (String) filterComboBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + query, 0, 1));
        }

        if (verdictFilter != null && !verdictFilter.startsWith("All")) {
            if (verdictFilter.contains("High")) {
                filters.add(RowFilter.regexFilter("(?i)HIGH", 5));
            } else if (verdictFilter.contains("Moderate")) {
                filters.add(RowFilter.regexFilter("(?i)MODERATE", 5));
            } else if (verdictFilter.contains("Low")) {
                filters.add(RowFilter.regexFilter("(?i)LOW", 5));
            }
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void inspectSelectedPair() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
        String docAName = (String) tableModel.getValueAt(modelRow, 0);
        String docBName = (String) tableModel.getValueAt(modelRow, 1);

        File fileA = scannedFilesMap.get(docAName);
        File fileB = scannedFilesMap.get(docBName);

        if (fileA != null && fileB != null) {
            loadFileIntoDocument(fileA, true);
            loadFileIntoDocument(fileB, false);
            tabbedPane.setSelectedIndex(0); // Switch to Side-by-Side tab
            runDirectComparison();
        }
    }

    private void exportReport(boolean asHtml) {
        if (lastResults.isEmpty()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String defaultName = asHtml ? "plagiarism_report.html" : "plagiarism_report.txt";
        chooser.setSelectedFile(new File(defaultName));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = chooser.getSelectedFile();
        try {
            String reportContent = asHtml ?
                    reportGenerator.buildHtmlReport(lastResults) :
                    reportGenerator.buildReport(lastResults);

            reportGenerator.exportToFile(reportContent, target.getAbsolutePath());
            statusLabel.setText("Report successfully saved to: " + target.getName());
            int openOption = JOptionPane.showConfirmDialog(this,
                    "Report exported successfully!\nWould you like to open it now?",
                    "Export Successful", JOptionPane.YES_NO_OPTION);
            if (openOption == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(target);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
