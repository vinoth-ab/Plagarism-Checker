import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for locating and reading text documents
 * from the "documents" directory so they can be compared for plagiarism.
 */
public class FileReaderUtil {

    // Supported extensions for readable plain-text and code documents.
    private static final String[] SUPPORTED_EXTENSIONS = {
            ".txt", ".text", ".md", ".java", ".py", ".c", ".cpp", ".cs",
            ".js", ".ts", ".html", ".css", ".json", ".xml", ".csv", ".log"
    };

    /**
     * Returns every supported document found in the given folder.
     *
     * @param folderPath path to the directory containing documents
     * @return list of File objects representing readable documents
     */
    public static List<File> listDocuments(String folderPath) {
        List<File> result = new ArrayList<>();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            return result;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return result;
        }

        for (File f : files) {
            if (f.isFile() && hasSupportedExtension(f.getName())) {
                result.add(f);
            }
        }
        return result;
    }

    public static boolean hasSupportedExtension(String fileName) {
        String lower = fileName.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS.clone();
    }

    /**
     * Reads the full contents of a file as a string (with fallback if non-UTF8).
     *
     * @param file the file to read
     * @return the file's text content
     * @throws IOException if the file cannot be read
     */
    public static String readFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }
}
