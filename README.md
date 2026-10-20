# 🛡️ Plagiarism Checker Pro

A modern, fast, and intuitive Java desktop application designed to detect and visualize text similarities, copy-pasting, and potential plagiarism across documents.

Built with an easy-to-use tabbed graphical user interface featuring **interactive side-by-side text comparison**, **real-time phrase highlighting**, **drag-and-drop support**, **batch folder scanning**, and **HTML/Text report generation**.

---

## ✨ Features

### 📄 1. Side-by-Side Direct Comparison
- **Direct Input & File Loading**: Compare two texts immediately by pasting content or opening files (`Document A` vs `Document B`).
- **Drag-and-Drop**: Drag text or code files directly from your file manager into either editor.
- **In-Text Highlight**: Overlapping phrases (n-gram shingles) are automatically highlighted directly in the document text boxes.
- **Matching Phrase Inspector**: Displays an interactive list of shared 3-word phrases; clicking any phrase scrolls to and highlights it in both documents.
- **Real-Time Word & Character Counters**: Dynamic counters for both source and comparison documents.
- **One-Click Swap (`⇄`)**: Instantly invert source and comparison documents.

### 📁 2. Folder Batch Scanner
- **Multi-Document Pairwise Analysis**: Scans all supported documents in a folder and analyzes every pair combination.
- **Summary Metric Cards**: Displays real-time stats for:
  - *Documents Scanned*
  - *Total Pairs Compared*
  - *Highest Similarity Score*
  - *Plagiarism Alerts (Scores ≥ 70%)*
- **Real-Time Search & Verdict Filter**: Instant search by document name and quick filtering by risk level (*High Risk*, *Moderate*, *Low*).
- **Visual Progress Bars & Badges**: Sortable table with color-coded percentage progress bars and pill badges.
- **Double-Click Row Inspection**: Double-clicking any row in the batch results automatically opens both files in the Side-by-Side comparison tab with full text and highlights loaded.

### 📊 3. Export Reports
- **Formatted Plain-Text Report (`.txt`)**: Complete summary with pairwise breakdown, scores, and verdicts.
- **Modern Styled HTML Report (`.html`)**: Beautiful, self-contained dashboard report with summary cards and colored badges, ready to open in any web browser or print to PDF.

### ⚙️ 4. Customizable Parameters
- Adjust the **Shingle N-Gram Size** (2–6 words, default: 3).
- Customize **High** (default: 70%) and **Moderate** (default: 40%) similarity alert thresholds.

### 📂 5. Expanded File Support
Supports plain text and popular source code formats:
`.txt`, `.text`, `.md`, `.java`, `.py`, `.c`, `.cpp`, `.cs`, `.js`, `.ts`, `.html`, `.css`, `.json`, `.xml`, `.csv`, `.log`.

---

## 🧠 How the Algorithm Works

The checker uses a blended multi-stage NLP algorithm combining two complementary techniques:

$$\text{Overall Score} = (0.4 \times \text{Cosine}) + (0.6 \times \text{Jaccard}) \times 100\%$$

1. **Text Normalization**:
   - Strips non-alphanumeric characters, converts to lowercase, tokenizes words, and filters out common stop words (`the`, `and`, `is`, etc.).

2. **Cosine Similarity (40% Weight - Vocabulary Distribution)**:
   - Evaluates word frequency vectors to determine if both documents discuss the same subject or draw from identical vocabulary distributions regardless of word order.

3. **Jaccard Shingle Overlap (60% Weight - Phrase Copying)**:
   - Deconstructs text into consecutive sequences of $N$ words (shingles/n-grams). Because word sequence is preserved, this provides a strong signal for directly copy-pasted or lightly paraphrased sentences.

4. **Verdict Levels**:
   - 🟢 **0–39%**: Low similarity (Unlikely plagiarism)
   - 🟡 **40–69%**: Moderate similarity (Review recommended)
   - 🔴 **70–100%**: High similarity (Likely plagiarism)

---

## 📁 Project Structure

```
PlagiarismChecker/
│
├── .gitignore                    - Ignores build artifacts and compiled classes
├── run.bat                       - One-click Windows batch launcher
├── run.ps1                       - PowerShell execution script
├── README.md                     - Repository documentation
│
└── PlagiarismChecker/
    ├── documents/                - Sample documents (sample1, sample2, sample3)
    └── src/
        ├── Main.java                 - Application entry point
        ├── GUI.java                  - Modern tabbed Swing user interface
        ├── UITheme.java              - UI styling, custom cards, buttons & cell renderers
        ├── FileReaderUtil.java       - Safe file loader & folder discovery
        ├── TextProcessor.java        - Text normalization, tokenizing & shingle generation
        ├── SimilarityCalculator.java - Cosine, Jaccard & phrase intersection math
        └── ReportGenerator.java      - Plain-text and styled HTML report builders
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK) 11** or newer (Tested up to JDK 22).

### Quick Run

- **Option A (PowerShell / Terminal)**:
  ```powershell
  .\run.ps1
  ```

- **Option B (Windows File Explorer)**:
  Double-click `run.bat`.

### Manual Build & Run

If running commands directly in your terminal:

```bash
# Navigate to the project directory
cd PlagiarismChecker

# Compile all source files into the out directory
javac -d out src/*.java

# Run the application
java -cp out Main
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
