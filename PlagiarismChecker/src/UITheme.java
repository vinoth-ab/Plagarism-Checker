import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Styling, modern colors, and custom component factories for the Plagiarism Checker UI.
 */
public class UITheme {

    // Modern Color Palette
    public static final Color BG_APP = new Color(248, 250, 252);       // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(226, 232, 240); // Slate 200
    public static final Color BORDER_ACTIVE = new Color(148, 163, 184);// Slate 400

    public static final Color PRIMARY = new Color(79, 70, 229);        // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);// Indigo 50

    public static final Color SECONDARY_BG = new Color(241, 245, 249); // Slate 100
    public static final Color SECONDARY_HOVER = new Color(226, 232, 240);

    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500

    // Severity Colors
    public static final Color HIGH_BG = new Color(254, 226, 226);      // Red 100
    public static final Color HIGH_FG = new Color(185, 28, 28);        // Red 700
    public static final Color HIGH_BAR = new Color(239, 68, 68);       // Red 500

    public static final Color MOD_BG = new Color(254, 243, 199);       // Amber 100
    public static final Color MOD_FG = new Color(180, 83, 9);          // Amber 700
    public static final Color MOD_BAR = new Color(245, 158, 11);       // Amber 500

    public static final Color LOW_BG = new Color(220, 252, 231);       // Green 100
    public static final Color LOW_FG = new Color(21, 128, 61);         // Green 700
    public static final Color LOW_BAR = new Color(16, 185, 129);       // Green 500

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Creates a primary pill-shaped or rounded modern button with hover transitions.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_HOVER);
                } else {
                    g2.setColor(PRIMARY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Creates a secondary outline/filled modern button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(SECONDARY_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(SECONDARY_HOVER);
                } else {
                    g2.setColor(SECONDARY_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_DARK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    /**
     * Creates a white card panel with a subtle border and padding.
     */
    public static JPanel createCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    /**
     * Creates an interactive drop target for dropping files/folders onto a component.
     */
    public static void setupDragAndDrop(JComponent component, Consumer<List<File>> onFilesDropped) {
        new DropTarget(component, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    if (dtde.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (droppedFiles != null && !droppedFiles.isEmpty()) {
                            onFilesDropped.accept(droppedFiles);
                            dtde.dropComplete(true);
                            return;
                        }
                    }
                    dtde.dropComplete(false);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                }
            }
        });
    }

    /**
     * Modern table cell renderer displaying a progress bar colored by plagiarism risk.
     */
    public static class SimilarityProgressRenderer extends DefaultTableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        private final JPanel panel = new JPanel(new BorderLayout(5, 0));
        private final JLabel percentLabel = new JLabel();

        public SimilarityProgressRenderer() {
            progressBar.setStringPainted(false);
            progressBar.setPreferredSize(new Dimension(80, 14));
            progressBar.setBorderPainted(false);
            progressBar.setBackground(new Color(226, 232, 240));

            percentLabel.setFont(FONT_BOLD);
            percentLabel.setPreferredSize(new Dimension(48, 14));

            panel.setOpaque(true);
            panel.add(progressBar, BorderLayout.CENTER);
            panel.add(percentLabel, BorderLayout.EAST);
            panel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            double score = 0.0;
            if (value instanceof Number) {
                score = ((Number) value).doubleValue();
            } else if (value != null) {
                try {
                    score = Double.parseDouble(value.toString().replace("%", "").trim());
                } catch (NumberFormatException ignored) {}
            }

            int valInt = (int) Math.round(score);
            progressBar.setValue(valInt);
            percentLabel.setText(String.format("%.1f%%", score));

            if (score >= 70.0) {
                progressBar.setForeground(HIGH_BAR);
                percentLabel.setForeground(HIGH_FG);
            } else if (score >= 40.0) {
                progressBar.setForeground(MOD_BAR);
                percentLabel.setForeground(MOD_FG);
            } else {
                progressBar.setForeground(LOW_BAR);
                percentLabel.setForeground(LOW_FG);
            }

            if (isSelected) {
                panel.setBackground(new Color(238, 242, 255));
            } else {
                panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }

            return panel;
        }
    }

    /**
     * Modern table cell renderer displaying a rounded pill badge for verdicts.
     */
    public static class VerdictBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String text = value != null ? value.toString() : "";
            JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            badgeContainer.setOpaque(true);
            badgeContainer.setBackground(isSelected ? new Color(238, 242, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

            JLabel badge = new JLabel(" " + text + " ") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setFont(FONT_BOLD);
            badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            if (text.toUpperCase().contains("HIGH")) {
                badge.setBackground(HIGH_BG);
                badge.setForeground(HIGH_FG);
            } else if (text.toUpperCase().contains("MODERATE")) {
                badge.setBackground(MOD_BG);
                badge.setForeground(MOD_FG);
            } else {
                badge.setBackground(LOW_BG);
                badge.setForeground(LOW_FG);
            }

            badgeContainer.add(badge);
            return badgeContainer;
        }
    }
}
