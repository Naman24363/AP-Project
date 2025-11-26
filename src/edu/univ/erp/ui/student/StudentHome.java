package edu.univ.erp.ui.student;

import edu.univ.erp.auth.Session;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.util.Ui;
import java.awt.*;
import javax.swing.*;

public class StudentHome extends JFrame {
    private final Session session;

    public StudentHome(Session s) {
        this.session = s;
        setTitle("Student Dashboard - " + s.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Ui.BG_LIGHT);
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content area with buttons
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(new Color(41, 128, 185)); // Professional blue
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        header.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Welcome, " + session.username);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Student Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(subtitleLabel);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(231, 76, 60)); // Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnLogout.addActionListener(e -> logout());

        header.add(leftPanel, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // Create button cards
        JPanel card1 = createButtonCard(
                "📚 Course Catalog",
                "Browse available courses\nand register for sections",
                () -> openCatalog());

        JPanel card2 = createButtonCard(
                "✓ My Registrations",
                "View your registered courses\nand drop sections",
                () -> openRegistrations());

        JPanel card3 = createButtonCard(
                "📅 My Timetable",
                "View your course schedule\nand timings",
                () -> openTimetable());

        JPanel card4 = createButtonCard(
                "📊 My Grades",
                "View your course grades\nand transcript",
                () -> openGrades());

        JPanel card5 = createButtonCard(
                "📄 Transcript",
                "Export your academic\ntranscript",
                () -> openTranscript());

        JPanel card6 = createButtonCard(
                "⚙ Settings",
                "Manage your account\nsettings",
                () -> openSettings());

        // Layout cards in a 2x3 grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        content.add(card1, gbc);

        gbc.gridx = 1;
        content.add(card2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        content.add(card3, gbc);

        gbc.gridx = 1;
        content.add(card4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        content.add(card5, gbc);

        gbc.gridx = 1;
        content.add(card6, gbc);

        return content;
    }

    private JPanel createButtonCard(String title, String description, Runnable action) {
        JPanel card = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 15;
                int w = getWidth();
                int h = getHeight();
                // fill background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // draw border
                g2.setColor(new Color(220, 220, 220));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(descLabel);
        card.add(Box.createVerticalGlue());

        // Add mouse listener for hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                        BorderFactory.createEmptyBorder(25, 25, 25, 25)));
                card.setBackground(new Color(245, 250, 255));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });

        return card;
    }

    private void openCatalog() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(0); // Catalog tab
        dash.setVisible(true);
        dispose();
    }

    private void openRegistrations() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(1); // My Registrations tab
        dash.setVisible(true);
        dispose();
    }

    private void openTimetable() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(2); // Timetable tab
        dash.setVisible(true);
        dispose();
    }

    private void openGrades() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(3); // Transcript tab
        dash.setVisible(true);
        dispose();
    }

    private void openTranscript() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(3); // Transcript tab
        dash.setVisible(true);
        dispose();
    }

    private void openSettings() {
        JFrame settingsFrame = new JFrame("Settings - " + session.username);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setSize(600, 700);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.add(new ChangePasswordPanel(session));
        settingsFrame.setVisible(true);
        dispose();
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
