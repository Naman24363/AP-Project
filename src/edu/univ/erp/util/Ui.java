package edu.univ.erp.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Ui {
    public static final Color PRIMARY = new Color(25, 118, 210);
    public static final Color PRIMARY_DARK = new Color(13, 71, 161);
    public static final Color PRIMARY_LIGHT = new Color(66, 165, 245);
    public static final Color ACCENT = new Color(0, 188, 212);
    public static final Color ACCENT_LIGHT = new Color(77, 208, 225);
    public static final Color SUCCESS = new Color(56, 142, 60);
    public static final Color SUCCESS_LIGHT = new Color(129, 199, 132);
    public static final Color ERROR = new Color(211, 47, 47);
    public static final Color ERROR_LIGHT = new Color(244, 67, 54);
    public static final Color WARNING = new Color(251, 140, 0);
    public static final Color WARNING_LIGHT = new Color(255, 152, 0);
    public static final Color INFO = new Color(0, 172, 193);
    public static final Color BG_LIGHT = new Color(248, 248, 248);
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color BG_SECONDARY = new Color(245, 245, 245);
    public static final Color TEXT_DARK = new Color(33, 33, 33);
    public static final Color TEXT_LIGHT = new Color(117, 117, 117);
    public static final Color TEXT_HINT = new Color(189, 189, 189);
    public static final Color DIVIDER = new Color(224, 224, 224);

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
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, h, PRIMARY_DARK);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Border
                g2.setColor(new Color(0, 0, 0, 20));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), 8, 8);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
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

        return b;
    }

    public static JButton buttonSecondary(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                if (getModel().isArmed()) {
                    g2.setColor(PRIMARY);
                } else {
                    g2.setColor(BG_LIGHT);
                }
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Border
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), 8, 8);

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(PRIMARY);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
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
                b.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setForeground(PRIMARY);
            }
        });

        return b;
    }

    public static JButton buttonDanger(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            private static final Color ERR_LIGHT = new Color(244, 67, 54);
            private static final Color ERR_DARK = new Color(211, 47, 47);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, ERR_LIGHT, 0, h, ERR_DARK);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                g2.setColor(new Color(0, 0, 0, 20));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), 8, 8);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
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

        return b;
    }

    public static JButton buttonSuccess(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            private static final Color SUCC_LIGHT = new Color(129, 199, 132);
            private static final Color SUCC_DARK = new Color(56, 142, 60);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, SUCC_LIGHT, 0, h, SUCC_DARK);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                g2.setColor(new Color(0, 0, 0, 20));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), 8, 8);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
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

        return b;
    }

    public static JButton tileButton(String text, Runnable onClick) {
        JButton b = new JButton("<html><div style='text-align:center;'><b>" + text + "</b></div></html>") {
            private int colorIndex = 0;
            private final Color[] gradientColors = {
                    PRIMARY_LIGHT, PRIMARY,
                    ACCENT, ACCENT_LIGHT,
                    SUCCESS_LIGHT, new Color(56, 142, 60)
            };

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                // Select colors based on button index for variety
                int colorIdx = (int) (Math.abs(getX() + getY()) / 100) % (gradientColors.length - 1);
                Color color1 = gradientColors[colorIdx];
                Color color2 = gradientColors[colorIdx + 1];

                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                // Add a subtle border
                g2.setColor(new Color(0, 0, 0, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), 14, 14);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.fillRoundRect(0, 0, w, h, 14, 14);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
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
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(248, 248, 248),
                        0, getHeight(), new Color(240, 240, 240));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Bottom accent line
                g2.setColor(PRIMARY_LIGHT);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight() - 3, getWidth(), getHeight() - 3);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel title = createLabelBold(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(PRIMARY_DARK);

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

    public static JPanel createCardPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle shadow effect
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2.dispose();
            }
        };
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
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
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                new EmptyBorder(6, 8, 6, 8)));
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                new EmptyBorder(6, 8, 6, 8)));
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(PRIMARY);
        return pf;
    }

    public static void showLoadingDialog(Component parent, String message) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(parent);

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(248, 248, 248),
                        0, getHeight(), new Color(240, 240, 240));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setForeground(TEXT_DARK);

        // Add spinner icon
        JLabel spinnerLabel = new JLabel("⟳");
        spinnerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        spinnerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        spinnerLabel.setForeground(PRIMARY);

        p.add(spinnerLabel, BorderLayout.NORTH);
        p.add(lbl, BorderLayout.CENTER);

        dialog.add(p);
        dialog.setVisible(true);
    }
}