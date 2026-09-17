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
│   ├── GUI.java                  - Swing user interface
│   ├── FileReaderUtil.java       - Finds and reads documents from disk
│   ├── TextProcessor.java        - Text normalization, tokenizing, shingling
│   ├── SimilarityCalculator.java - Cosine & Jaccard similarity math
│   └── ReportGenerator.java      - Builds and exports the report
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
4. **GUI** lets you pick the documents folder, run the comparison, view
   results in a sortable table, and export a plain-text report.
5. **ReportGenerator** formats results into a report with a verdict for
   each pair:
   - **0–39%** → Low similarity
   - **40–69%** → Moderate similarity, review recommended
   - **70–100%** → High similarity, likely plagiarism

## Requirements

- Java Development Kit (JDK) 11 or newer

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
