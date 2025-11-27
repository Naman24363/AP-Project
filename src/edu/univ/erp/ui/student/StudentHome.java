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
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel card = Ui.createPanel(new BorderLayout(), Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);

        JButton bCatalog = Ui.tileButton("Course Catalog", this::openCatalog);
        JButton bRegs = Ui.tileButton("My Registrations", this::openRegistrations);
        JButton bTime = Ui.tileButton("My Timetable", this::openTimetable);
        JButton bGrades = Ui.tileButton("My Grades", this::openGrades);
        JButton bTrans = Ui.tileButton("View Grades", this::openTranscript);
        JButton bSettings = Ui.tileButton("Settings", this::openSettings);

        Dimension smallTile = new Dimension(300, 140);
        bCatalog.setPreferredSize(smallTile);
        bRegs.setPreferredSize(smallTile);
        bTime.setPreferredSize(smallTile);
        bGrades.setPreferredSize(smallTile);
        bTrans.setPreferredSize(smallTile);
        bSettings.setPreferredSize(smallTile);

        grid.add(bCatalog);
        grid.add(bRegs);
        grid.add(bTime);
        grid.add(bGrades);
        grid.add(bTrans);
        grid.add(bSettings);

        card.add(grid, BorderLayout.CENTER);

        wrap.add(card);
        return wrap;
    }

    // Using Ui.tileButton for consistent look with Admin/Instructor dashboards

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
        dash.selectTab(3); // View Grades tab
        dash.setVisible(true);
        dispose();
    }

    private void openTranscript() {
        StudentDashboard dash = new StudentDashboard(session);
        dash.selectTab(3); // View Grades tab
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
