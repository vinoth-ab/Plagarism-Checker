import javax.swing.SwingUtilities;

/**
 * Entry point for the Plagiarism Checker application.
 * Launches the Swing GUI on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}
