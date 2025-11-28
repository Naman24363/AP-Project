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
        JPanel header = new JPanel();
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        header.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Welcome, " + session.username);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Instructor Dashboard");
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
