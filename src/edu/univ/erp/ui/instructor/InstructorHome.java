package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.Session;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.util.Ui;
import java.awt.*;
import javax.swing.*;

public class InstructorHome extends JFrame {
    private final Session session;

    public InstructorHome(Session s) {
        this.session = s;
        setTitle("Instructor Dashboard - " + s.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Ui.BG_LIGHT);
        mainPanel.setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background with vibrant colors
                GradientPaint gp = new GradientPaint(0, 0, new Color(251, 140, 0),
                        getWidth(), getHeight(), new Color(25, 118, 210));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle accent stripe
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRect(0, 0, getWidth(), 3);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        header.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Welcome, " + session.username);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Instructor Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(240, 240, 240));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(subtitleLabel);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(244, 67, 54));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());

        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(229, 57, 53));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogout.setBackground(new Color(244, 67, 54));
            }
        });

        header.add(leftPanel, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel card = Ui.createPanel(new BorderLayout(), Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);

        JButton bSections = Ui.tileButton("My Sections", this::openSections);
        JButton bGrades = Ui.tileButton("Manage Grades", this::openGrades);
        JButton bStats = Ui.tileButton("Class Stats", this::openStats);
        JButton bImport = Ui.tileButton("Import Grades", this::openImportGrades);
        JButton bExport = Ui.tileButton("Export Grades", this::openExportGrades);
        JButton bSettings = Ui.tileButton("Settings", this::openSettings);

        Dimension smallTile = new Dimension(200, 140);
        bSections.setPreferredSize(smallTile);
        bGrades.setPreferredSize(smallTile);
        bStats.setPreferredSize(smallTile);
        bImport.setPreferredSize(smallTile);
        bExport.setPreferredSize(smallTile);
        bSettings.setPreferredSize(smallTile);

        grid.add(bSections);
        grid.add(bGrades);
        grid.add(bStats);
        grid.add(bImport);
        grid.add(bExport);
        grid.add(bSettings);

        card.add(grid, BorderLayout.CENTER);

        wrap.add(card);
        return wrap;
    }

    private void openSections() {
        InstructorDashboard dash = new InstructorDashboard(session);
        dash.selectTab(0);
        dash.setVisible(true);
        dispose();
    }

    private void openGrades() {
        InstructorDashboard dash = new InstructorDashboard(session);
        dash.selectTab(1);
        dash.setVisible(true);
        dispose();
    }

    private void openStats() {
        InstructorDashboard dash = new InstructorDashboard(session);
        dash.selectTab(2);
        dash.setVisible(true);
        dispose();
    }

    private void openImportGrades() {
        InstructorDashboard dash = new InstructorDashboard(session);
        dash.selectTab(1);
        dash.setVisible(true);
        dispose();
    }

    private void openExportGrades() {
        InstructorDashboard dash = new InstructorDashboard(session);
        dash.selectTab(1);
        dash.setVisible(true);
        dispose();
    }

    private void openSettings() {
        JFrame settingsFrame = new JFrame("Settings - " + session.username);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setSize(600, 700);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.add(new edu.univ.erp.ui.student.ChangePasswordPanel(session));
        settingsFrame.setVisible(true);
        dispose();
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
