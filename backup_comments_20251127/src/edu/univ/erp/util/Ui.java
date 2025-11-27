package edu.univ.erp.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Ui {
    public static final Color PRIMARY = new Color(25, 118, 210); // Deep Blue
    public static final Color PRIMARY_DARK = new Color(13, 71, 161); // Darker Blue
    public static final Color PRIMARY_LIGHT = new Color(66, 165, 245); // Light Blue
    public static final Color ACCENT = new Color(0, 188, 212); // Cyan
    public static final Color SUCCESS = new Color(56, 142, 60); // Green
    public static final Color ERROR = new Color(211, 47, 47); // Red
    public static final Color ERROR_LIGHT = new Color(244, 67, 54); // Light Red
    public static final Color WARNING = new Color(251, 140, 0); // Orange
    public static final Color BG_LIGHT = new Color(248, 248, 248); // Almost white
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(33, 33, 33); // Dark text
    public static final Color TEXT_LIGHT = new Color(117, 117, 117); // Medium Gray
    public static final Color TEXT_HINT = new Color(189, 189, 189); // Light Gray
    public static final Color DIVIDER = new Color(224, 224, 224); // Divider line

    static {
        try {
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (Exception ignored) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }

            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 12);

            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", boldFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("ComboBox.font", defaultFont);
            UIManager.put("TabbedPane.font", boldFont);

            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusedBackground", PRIMARY_DARK);
            UIManager.put("Table.background", BG_SURFACE);
            UIManager.put("Table.alternateRowColor", BG_LIGHT);
            UIManager.put("Table.gridColor", DIVIDER);
            UIManager.put("TabbedPane.background", BG_SURFACE);
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
    }

    public static void init() {
    }

    public static void msgInfo(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "ℹ Info", JOptionPane.INFORMATION_MESSAGE);
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

    public static JButton button(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(b);
                msgError(topFrame != null ? topFrame : b, ex.getMessage());
            }
        });

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(PRIMARY_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(PRIMARY);
            }
        });

        return b;
    }

    public static JButton buttonSecondary(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(PRIMARY);
        b.setBackground(BG_LIGHT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(b);
                msgError(topFrame != null ? topFrame : b, ex.getMessage());
            }
        });

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

        return b;
    }

    public static JButton buttonDanger(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(ERROR);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(b);
                msgError(topFrame != null ? topFrame : b, ex.getMessage());
            }
        });

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(229, 57, 53));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(ERROR);
            }
        });

        return b;
    }

    public static JButton buttonSuccess(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(SUCCESS);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(b);
                msgError(topFrame != null ? topFrame : b, ex.getMessage());
            }
        });

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(46, 125, 50));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(SUCCESS);
            }
        });

        return b;
    }

    public static JButton tileButton(String text, Runnable onClick) {
        JButton b = new JButton("<html><div style='text-align:center;'><b>" + text + "</b></div></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_LIGHT, 0, h, PRIMARY_DARK);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, 14, 14);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200, 120));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);

        b.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(b);
                msgError(topFrame != null ? topFrame : b, ex.getMessage());
            }
        });

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
            }
        });

        return b;
    }

    public static JPanel headerPanel(String titleText, String subtitleText) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = createLabelBold(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = createLabel(subtitleText);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_LIGHT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    public static JPanel createPanel(LayoutManager layout, Color background) {
        JPanel p = new JPanel(layout);
        p.setBackground(background);
        return p;
    }

    public static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel createLabelBold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 189, 189), 1),
                new EmptyBorder(5, 5, 5, 5)));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 189, 189), 1),
                new EmptyBorder(5, 5, 5, 5)));
        return pf;
    }

    public static void showLoadingDialog(Component parent, String message) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl, BorderLayout.CENTER);

        dialog.add(p);
        dialog.setVisible(true);
    }
}
