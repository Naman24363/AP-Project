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
        JPanel header = new JPanel();
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        header.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Welcome, " + session.username);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Admin Dashboard");
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
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnLogout.addActionListener(e -> logout());

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
