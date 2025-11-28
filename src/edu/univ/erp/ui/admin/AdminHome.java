package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.Session;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.util.Ui;
import java.awt.*;
import javax.swing.*;

public class AdminHome extends JFrame {
    private final Session session;

    public AdminHome(Session s) {
        this.session = s;
        setTitle("Admin Dashboard - " + s.username);
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 118, 210),
                        getWidth(), getHeight(), new Color(0, 150, 170));
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

        JLabel subtitleLabel = new JLabel("Admin Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(220, 230, 240));

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

        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));
        grid.setOpaque(false);

        JButton bUsers = Ui.tileButton("Create/Edit Users", this::openUsers);
        JButton bCourses = Ui.tileButton("Create/Edit Courses", this::openCourses);
        JButton bSections = Ui.tileButton("Create Sections", this::openSections);
        JButton bMaint = Ui.tileButton("Maintenance", this::openMaintenance);
        JButton bCatalog = Ui.tileButton("View Catalog", this::openCatalog);
        JButton bBackup = Ui.tileButton("Backup & Restore", this::openBackupRestore);
        JButton bSettings = Ui.tileButton("Settings", this::openSettings);

        Dimension smallTile = new Dimension(200, 140);
        bUsers.setPreferredSize(smallTile);
        bCourses.setPreferredSize(smallTile);
        bSections.setPreferredSize(smallTile);
        bMaint.setPreferredSize(smallTile);
        bCatalog.setPreferredSize(smallTile);
        bBackup.setPreferredSize(smallTile);
        bSettings.setPreferredSize(smallTile);

        grid.add(bUsers);
        grid.add(bCourses);
        grid.add(bSections);
        grid.add(bMaint);
        grid.add(bCatalog);
        grid.add(bBackup);
        grid.add(bSettings);

        card.add(grid, BorderLayout.CENTER);

        wrap.add(card);
        return wrap;
    }

    private void openUsers() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(2);
        dash.setVisible(true);
        dispose();
    }

    private void openCourses() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(3);
        dash.setVisible(true);
        dispose();
    }

    private void openSections() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(4);
        dash.setVisible(true);
        dispose();
    }

    private void openMaintenance() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(1);
        dash.setVisible(true);
        dispose();
    }

    private void openCatalog() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(5);
        dash.setVisible(true);
        dispose();
    }

    private void openBackupRestore() {
        AdminDashboard dash = new AdminDashboard(session);
        dash.selectTab(0);
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
