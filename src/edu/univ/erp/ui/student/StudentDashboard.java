package edu.univ.erp.ui.student;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudentDashboard extends JFrame {
    private final Session session;
    private final CatalogService catalog = new CatalogService();
    private final StudentService student = new StudentService();

    private final JTable tblCatalog = new JTable();
    private final JTable tblRegs = new JTable();
    private JTabbedPane tabs;

    public StudentDashboard(Session s) {
        this.session = s;
        setTitle("Student Dashboard - " + s.username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top header (welcome + subtitle + logout) to match instructor style
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        JLabel lblWelcome = new JLabel("Welcome, " + s.username);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(Color.WHITE);
        left.add(lblWelcome);
        JLabel lblSub = new JLabel("Student Dashboard");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 230, 250));
        left.add(lblSub);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(244, 81, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnLogout.addActionListener(e -> {
            dispose();
            new edu.univ.erp.ui.auth.LoginFrame().setVisible(true);
        });

        topBar.add(left, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        tabs = new JTabbedPane();
        tabs.add("Catalog", catalogPanel());
        tabs.add("My Registrations", regsPanel());
        tabs.add("Timetable", new TimetablePanel(s));
        tabs.add("View Grades", transcriptPanel());
        tabs.add("Settings", new ChangePasswordPanel(s));

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
        refreshAll();
    }

    private void goBackToHome() {
        dispose();
        new StudentHome(session).setVisible(true);
    }

    public void selectTab(int index) {
        if (tabs != null && index >= 0 && index < tabs.getTabCount()) {
            tabs.setSelectedIndex(index);
        }
    }

    // Removed studentLandingPanel: students will now see Catalog as the first tab
    // after login.

    private JPanel catalogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(tblCatalog), BorderLayout.CENTER);
        JPanel south = new JPanel();
        JButton btnRefresh = Ui.button("Refresh", this::refreshCatalog);
        JButton btnRegister = Ui.button("Register Selected", () -> {
            int r = tblCatalog.getSelectedRow();
            if (r < 0) {
                Ui.msgError(this, "Select a section.");
                return;
            }
            int sectionId = (int) tblCatalog.getValueAt(r, 0);
            try {
                student.register(session, sectionId);
                Ui.msgInfo(this, "Registered.");
                refreshAll();
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });
        south.add(btnRefresh);
        south.add(btnRegister);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel regsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(tblRegs), BorderLayout.CENTER);
        JPanel south = new JPanel();
        JButton btnRefresh = Ui.button("Refresh", this::refreshRegs);
        JButton btnDrop = Ui.button("Drop Selected", () -> {
            int r = tblRegs.getSelectedRow();
            if (r < 0) {
                Ui.msgError(this, "Select a registration.");
                return;
            }
            int enrollmentId = (int) tblRegs.getValueAt(r, 0);
            try {
                student.drop(session, enrollmentId);
                Ui.msgInfo(this, "Dropped.");
                refreshAll();
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });
        south.add(btnRefresh);
        south.add(btnDrop);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel transcriptPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGrades = Ui.button("View Grades", () -> {
            try {
                DefaultTableModel m = student.grades(session.userId);
                JTable t = new JTable(m);
                JOptionPane.showMessageDialog(this, new JScrollPane(t), "Grades", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });
        JButton btnCsv = Ui.button("Export Transcript CSV", () -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("transcript_" + session.username + ".csv"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    student.exportTranscriptCsv(session.userId, fc.getSelectedFile().getAbsolutePath());
                    Ui.msgInfo(this, "Saved: " + fc.getSelectedFile().getAbsolutePath());
                } catch (Exception e) {
                    Ui.msgError(this, e.getMessage());
                }
            }
        });
        top.add(btnGrades);
        top.add(btnCsv);
        p.add(top, BorderLayout.NORTH);
        return p;
    }

    private void refreshCatalog() {
        try {
            tblCatalog.setModel(catalog.listCatalog());
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void refreshRegs() {
        try {
            tblRegs.setModel(student.myRegistrations(session.userId));
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void refreshAll() {
        refreshCatalog();
        refreshRegs();
    }
}
