# Plagiarism Checker

A simple Java desktop application that compares text documents against
one another and reports how similar they are, as a way of flagging
potential plagiarism.

## Project Structure

```
PlagiarismChecker
│
├── src
│   ├── Main.java                 - Application entry point
│   ├── GUI.java                  - Modern tabbed Swing user interface
│   ├── UITheme.java              - Custom colors, buttons, cards & table renderers
│   ├── FileReaderUtil.java       - Finds and reads documents from disk
│   ├── TextProcessor.java        - Text normalization, tokenizing, shingling
│   ├── SimilarityCalculator.java - Cosine & Jaccard similarity math
│   └── ReportGenerator.java      - Builds and exports text and HTML reports
│
├── documents/                    - Drop the files you want to compare here
└── README.md
```

## How It Works

1. **FileReaderUtil** scans the `documents` folder for `.txt`, `.text`,
   and `.md` files and reads their raw content.
2. **TextProcessor** normalizes each document (lowercase, strip
   punctuation), tokenizes it into words, removes common stop words,
   and builds:
   - a **word-frequency map** (for cosine similarity)
   - a set of **word shingles** / n-grams (for phrase-level overlap)
3. **SimilarityCalculator** compares every pair of documents using:
   - **Cosine similarity** — how similar the overall word usage is
   - **Jaccard similarity** — how much exact 3-word phrase overlap exists
   - These are blended into a single **overall similarity score**
     (0–100%), weighted more toward phrase overlap since that's the
     stronger plagiarism signal.
## Key Features & Usability Enhancements

1. **Side-by-Side Comparison (Direct Text / Files)**:
   - Compare two texts directly by pasting text or loading files with "Open File..."
   - **Drag-and-Drop**: Drag any text file from your file explorer directly into Document A or B.
   - **Interactive Overlap Highlighting**: Automatically highlights copied/matching phrases in yellow directly in the document text boxes.
   - **Matching Phrase Inspector**: Displays the exact matching 3-word n-gram shingles found in both texts; clicking any phrase highlights it instantly.
   - **Word & Character Counters** and **One-Click Swap (⇄)**.

2. **Folder Batch Scanner**:
   - Scans and compares all documents in a directory pairwise.
   - **Interactive Stat Cards**: Displays Total Documents, Comparisons Analyzed, Highest Similarity, and Plagiarism Alerts (≥70%).
   - **Instant Search & Risk Filtering**: Search documents in real time and filter by risk level (High, Moderate, Low).
   - **Visual Progress Bars & Badges**: Clean percentage progress bars and colored pill badges in the results table.
   - **Double-Click Row Inspection**: Double-clicking any pair in the table loads them straight into the Side-by-Side comparison tab with highlights!

3. **Multi-Format Export**:
   - Export reports as plain-text (`.txt`) or styled modern web reports (`.html`).

4. **Expanded File Support**:
   - Supports `.txt`, `.text`, `.md`, `.java`, `.py`, `.c`, `.cpp`, `.cs`, `.js`, `.ts`, `.html`, `.css`, `.json`, `.csv`, `.log`.

## Requirements

- Java Development Kit (JDK) 11 or newer (Tested on JDK 22)

## Build & Run

From the project root:

```bash
# Compile
javac -d out src/*.java

# Run
java -cp out Main
```

A window will open with a text field pointing at the `documents`
folder (relative paths work if you run the command from the project
root). Click **Browse...** to pick a different folder, then
**Run Check** to compare every document inside it pairwise. Results
appear in the table, and **Export Report...** saves a formatted
`.txt` report to a location of your choice.

## Sample Documents

Three sample files are included in `documents/` so you can try the
tool immediately:

- `sample1.txt` and `sample2.txt` — both about AI, with intentionally
  overlapping phrasing to demonstrate a moderate/high similarity score
- `sample3.txt` — an unrelated pizza recipe, which should score near 0%
  against the other two

Feel free to delete these and drop in your own files.

## Notes & Possible Extensions

- Currently supports `.txt`, `.text`, and `.md` files. Support for
  `.docx` or `.pdf` input could be added by extending
  `FileReaderUtil` with a library like Apache POI or PDFBox.
- The shingle size (default 3 words) can be tuned in `GUI.java`
  (`SHINGLE_SIZE` constant) — smaller values catch more overlap but
  produce more false positives.
- The stop-word list in `TextProcessor` is intentionally small; a
  larger list can be substituted for more aggressive filtering.
