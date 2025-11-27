package edu.univ.erp.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * UI helper methods for creating styled components and showing dialogs.
 */
public class UiHelper {
    private static final Color PRIMARY = UiConstants.PRIMARY;
    private static final Color PRIMARY_DARK = UiConstants.PRIMARY_DARK;
    private static final Color ERROR = UiConstants.ERROR;
    private static final Color SUCCESS = UiConstants.SUCCESS;
    private static final Color WARNING = UiConstants.WARNING;
    private static final Color TEXT_DARK = UiConstants.TEXT_DARK;
    private static final Color BG_LIGHT = UiConstants.BG_LIGHT;

    static {
        initializeLookAndFeel();
    }

    private static void initializeLookAndFeel() {
        try {
            // Try FlatLaf first, then fall back to Nimbus
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (Exception ignored) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }

            // Configure UI defaults
            UIManager.put("Label.font", UiConstants.FONT_REGULAR);
            UIManager.put("Button.font", UiConstants.FONT_BOLD);
            UIManager.put("TextField.font", UiConstants.FONT_REGULAR);
            UIManager.put("TextArea.font", UiConstants.FONT_REGULAR);
            UIManager.put("Table.font", UiConstants.FONT_REGULAR);
            UIManager.put("ComboBox.font", UiConstants.FONT_REGULAR);
            UIManager.put("TabbedPane.font", UiConstants.FONT_BOLD);

            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.gridColor", UiConstants.DIVIDER);
        } catch (Exception e) {
            System.err.println("Failed to initialize look and feel: " + e.getMessage());
        }
    }

    // ===== Dialog Messages =====

    public static void msgInfo(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "ℹ Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void msgError(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "✗ Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void msgSuccess(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "✓ Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void msgWarning(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "⚠ Warning", JOptionPane.WARNING_MESSAGE);
    }

    // ===== Buttons =====

    public static JButton button(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        b.addActionListener(e -> safeClick(b, onClick));
        addHoverEffect(b, PRIMARY, PRIMARY_DARK);
        return b;
    }

    public static JButton buttonSecondary(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, BG_LIGHT, PRIMARY);
        b.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        b.addActionListener(e -> safeClick(b, onClick));
        addHoverEffectSecondary(b);
        return b;
    }

    public static JButton buttonDanger(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, ERROR, Color.WHITE);
        b.addActionListener(e -> safeClick(b, onClick));
        addHoverEffect(b, ERROR, new Color(229, 57, 53));
        return b;
    }

    public static JButton buttonSuccess(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, SUCCESS, Color.WHITE);
        b.addActionListener(e -> safeClick(b, onClick));
        addHoverEffect(b, SUCCESS, new Color(46, 125, 50));
        return b;
    }

    private static void styleButton(JButton b, Color background, Color foreground) {
        b.setFont(UiConstants.FONT_BOLD);
        b.setForeground(foreground);
        b.setBackground(background);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void addHoverEffect(JButton b, Color normal, Color hover) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(normal);
            }
        });
    }

    private static void addHoverEffectSecondary(JButton b) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(PRIMARY);
                b.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(BG_LIGHT);
                b.setForeground(PRIMARY);
            }
        });
    }

    private static void safeClick(Component parent, Runnable onClick) {
        try {
            onClick.run();
        } catch (Exception ex) {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
            msgError(topFrame != null ? topFrame : parent, ex.getMessage());
        }
    }

    // ===== Labels =====

    public static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiConstants.FONT_REGULAR);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel createLabelBold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiConstants.FONT_BOLD);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel createLabelTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiConstants.FONT_TITLE);
        l.setForeground(PRIMARY);
        return l;
    }

    // ===== Text Fields =====

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UiConstants.FONT_REGULAR);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiConstants.TEXT_HINT, 1),
                new EmptyBorder(5, 5, 5, 5)));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(UiConstants.FONT_REGULAR);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiConstants.TEXT_HINT, 1),
                new EmptyBorder(5, 5, 5, 5)));
        return pf;
    }

    // ===== Panels =====

    public static JPanel createPanel(LayoutManager layout, Color background) {
        JPanel p = new JPanel(layout);
        p.setBackground(background);
        return p;
    }

    public static JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(UiConstants.DIVIDER, 1));
        return p;
    }

    // ===== Dialogs =====

    public static void showLoadingDialog(Component parent, String message) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(message);
        lbl.setFont(UiConstants.FONT_REGULAR);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl, BorderLayout.CENTER);

        dialog.add(p);
        dialog.setVisible(true);
    }
}
