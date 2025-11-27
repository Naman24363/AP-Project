package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.AuthDb;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class InstructorDashboard extends JFrame {
    private final Session session;
    private final InstructorService instructor = new InstructorService();
    private final JTable tblSections = new JTable();
    private final JTable tblRoster = new JTable();
    private final JTable tblStats = new JTable();
    private int currentRosterSectionId = -1;
    private JTabbedPane tabs;

    public InstructorDashboard(Session s) {
        this.session = s;
        setTitle("Instructor Dashboard - " + s.username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        JLabel lblWelcome = new JLabel("Welcome, " + s.username);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(Color.WHITE);
        left.add(lblWelcome);
        JLabel lblSub = new JLabel("Instructor Dashboard");
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
            new LoginFrame().setVisible(true);
        });

        topBar.add(left, BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);

        this.tabs = new JTabbedPane();
        tabs.add("Dashboard", instructorLandingPanel());
        tabs.add("My Sections", sectionsPanel());
        tabs.add("Grades", gradesPanel());
        tabs.add("Class Stats", statsPanel());
        tabs.add("Settings", settingsPanel());

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);
        add(main);
        refreshSections();
    }

    private void showChangePasswordDialog() {
        JDialog d = new JDialog(this, "Change Password", true);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        p.add(Ui.createLabelBold("Change Password"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("Current Password"), gbc);
        gbc.gridx = 1;
        JPasswordField pfOld = Ui.createPasswordField(20);
        p.add(pfOld, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("New Password"), gbc);
        gbc.gridx = 1;
        JPasswordField pfNew = Ui.createPasswordField(20);
        p.add(pfNew, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("Confirm New Password"), gbc);
        gbc.gridx = 1;
        JPasswordField pfConfirm = Ui.createPasswordField(20);
        p.add(pfConfirm, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        JButton btnChange = Ui.button("Change", () -> {
            String oldP = new String(pfOld.getPassword());
            String n = new String(pfNew.getPassword());
            String c = new String(pfConfirm.getPassword());
            if (oldP.isEmpty() || n.isEmpty() || c.isEmpty()) {
                Ui.msgError(d, "All fields required");
                return;
            }
            if (!n.equals(c)) {
                Ui.msgError(d, "New passwords do not match");
                return;
            }
            if (n.length() < 8) {
                Ui.msgError(d, "Password must be at least 8 characters");
                return;
            }
            try {
                boolean ok = false;
                try (java.sql.Connection conn = AuthDb.get();
                        java.sql.PreparedStatement ps = conn
                                .prepareStatement("SELECT password_hash FROM users_auth WHERE user_id = ?")) {
                    ps.setInt(1, session.userId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String hash = rs.getString(1);
                            ok = PasswordHasher.verify(oldP, hash);
                        }
                    }
                }
                if (!ok) {
                    Ui.msgError(d, "Current password incorrect");
                    return;
                }

                String newHash = PasswordHasher.hash(n);
                try (java.sql.Connection conn = AuthDb.get();
                        java.sql.PreparedStatement ps = conn
                                .prepareStatement("UPDATE users_auth SET password_hash = ? WHERE user_id = ?")) {
                    ps.setString(1, newHash);
                    ps.setInt(2, session.userId);
                    ps.executeUpdate();
                }

                Ui.msgSuccess(d, "Password changed");
                d.dispose();
            } catch (Exception ex) {
                Ui.msgError(d, ex.getMessage());
            }
        });
        JButton btnCancel = Ui.buttonSecondary("Cancel", () -> d.dispose());
        btns.add(btnChange);
        btns.add(btnCancel);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        p.add(btns, gbc);

        d.getContentPane().add(p);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JPanel instructorLandingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));

        java.util.function.Consumer<String> openTab = (name) -> {
            if (InstructorDashboard.this.tabs == null)
                return;
            for (int i = 0; i < InstructorDashboard.this.tabs.getTabCount(); i++) {
                if (InstructorDashboard.this.tabs.getTitleAt(i).equalsIgnoreCase(name)) {
                    InstructorDashboard.this.tabs.setSelectedIndex(i);
                    return;
                }
            }
        };

        JButton bSections = Ui.tileButton("My Sections", () -> openTab.accept("My Sections"));
        JButton bGrades = Ui.tileButton("Grades", () -> openTab.accept("Grades"));
        JButton bStats = Ui.tileButton("Class Stats", () -> openTab.accept("Class Stats"));
        JButton bImport = Ui.tileButton("Import Grades", () -> openTab.accept("Grades"));
        JButton bExport = Ui.tileButton("Export Grades", () -> openTab.accept("Grades"));
        JButton bSettings = Ui.tileButton("Settings", () -> openTab.accept("Settings"));

        Dimension smallTile = new Dimension(140, 84);
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

        JPanel card = Ui.createPanel(new BorderLayout(), Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
                new EmptyBorder(18, 18, 18, 18)));
        card.add(grid, BorderLayout.CENTER);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(Ui.BG_LIGHT);
        centerWrap.add(card);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 0, 20));
        JLabel title = Ui.createLabelBold("Instructor Dashboard");
        JLabel subtitle = Ui.createLabel("Quick instructor actions");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        subtitle.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        p.add(header, BorderLayout.NORTH);
        p.add(centerWrap, BorderLayout.CENTER);
        return p;
    }

    private JPanel sectionsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = Ui.button("Refresh", this::refreshSections);
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tblSections), BorderLayout.CENTER);
        return p;
    }

    private JPanel gradesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(Ui.createLabel("Section ID:"));
        JTextField txtSection = Ui.createTextField(6);
        top.add(txtSection);
        JButton btnLoad = Ui.button("Load Students", () -> {
            try {
                int sid = Integer.parseInt(txtSection.getText().trim());
                loadRoster(sid);
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid Section ID");
            }
        });
        JButton btnExport = Ui.button("Export CSV", () -> {
            try {
                int sid = Integer.parseInt(txtSection.getText().trim());
                exportGrades(sid);
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid Section ID");
            }
        });
        JButton btnImport = Ui.button("Import CSV", () -> {
            try {
                int sid = Integer.parseInt(txtSection.getText().trim());
                importGrades(sid);
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid Section ID");
            }
        });
        JButton btnSaveSelected = Ui.button("Save Selected", () -> saveSelected());
        JButton btnSaveAll = Ui.button("Save All", () -> saveAll());
        JButton btnSetWeights = Ui.button("Set Weights", () -> {
            try {
                int sid = Integer.parseInt(txtSection.getText().trim());
                setWeights(sid);
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid Section ID");
            }
        });
        top.add(btnLoad);
        top.add(btnExport);
        top.add(btnSaveSelected);
        top.add(btnSaveAll);
        top.add(btnSetWeights);
        top.add(btnImport);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tblRoster), BorderLayout.CENTER);
        return p;
    }

    private JPanel statsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(Ui.createLabel("Section ID:"));
        JTextField txtSection2 = Ui.createTextField(6);
        top.add(txtSection2);
        JButton btnLoad = Ui.button("Load Stats", () -> {
            try {
                int sid = Integer.parseInt(txtSection2.getText().trim());
                loadStats(sid);
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid Section ID");
            }
        });
        top.add(btnLoad);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tblStats), BorderLayout.CENTER);
        return p;
    }

    private void refreshSections() {
        try {
            tblSections.setModel(instructor.mySections(session.userId));
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void loadRoster(int sectionId) {
        try {
            DefaultTableModel base = instructor.roster(session, sectionId);
            int cols = base.getColumnCount();

            java.util.Set<Integer> studentIds = new java.util.HashSet<>();
            for (int r = 0; r < base.getRowCount(); r++) {
                Object idObj = base.getValueAt(r, 1);
                if (idObj instanceof Integer) {
                    studentIds.add((Integer) idObj);
                } else if (idObj != null) {
                    try {
                        studentIds.add(Integer.parseInt(idObj.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            java.util.Map<Integer, String> names = new java.util.HashMap<>();
            try {
                names.putAll(edu.univ.erp.data.AuthLookup.usernamesForIds(studentIds));
            } catch (SQLException ex) {
                System.err.println("Auth lookup failed: " + ex.getMessage());
            }

            String[] colNames = new String[cols + 1];
            for (int i = 0; i < cols; i++) {
                String n = base.getColumnName(i);
                if ("Student ID".equalsIgnoreCase(n))
                    n = "Student Name";
                if ("Enrollment ID".equalsIgnoreCase(n))
                    n = "Section ID";
                colNames[i] = n;
            }
            colNames[cols] = "_enrollment_id";

            DefaultTableModel editable = new DefaultTableModel(colNames, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return c == 3 || c == 4 || c == 5;
                }
            };

            for (int r = 0; r < base.getRowCount(); r++) {
                Object[] row = new Object[cols + 1];
                for (int c = 0; c < cols; c++) {
                    Object val = base.getValueAt(r, c);
                    if (c == 0) {
                        row[0] = sectionId;
                        row[cols] = val;
                    } else if (c == 1) {
                        String name = "";
                        if (val instanceof Integer) {
                            name = names.getOrDefault((Integer) val, val.toString());
                        } else if (val != null) {
                            try {
                                int uid = Integer.parseInt(val.toString());
                                name = names.getOrDefault(uid, val.toString());
                            } catch (NumberFormatException nfe) {
                                name = val.toString();
                            }
                        }
                        row[c] = name;
                    } else {
                        row[c] = val;
                    }
                }
                editable.addRow(row);
            }

            tblRoster.setModel(editable);
            currentRosterSectionId = sectionId;
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void saveSelected() {
        int row = tblRoster.getSelectedRow();
        if (row == -1) {
            Ui.msgError(this, "Please select a student row to save.");
            return;
        }
        if (currentRosterSectionId == -1) {
            Ui.msgError(this, "No roster loaded.");
            return;
        }

        try {
            int lastCol = tblRoster.getColumnCount() - 1;
            Object eidObj = tblRoster.getValueAt(row, lastCol);
            if (eidObj == null)
                throw new RuntimeException("Enrollment ID missing");
            int enrollmentId = (int) eidObj;

            Double quiz = parseDoubleOrNull(tblRoster.getValueAt(row, 3));
            Double midterm = parseDoubleOrNull(tblRoster.getValueAt(row, 4));
            Double endsem = parseDoubleOrNull(tblRoster.getValueAt(row, 5));

            instructor.saveScores(session, currentRosterSectionId, enrollmentId, quiz, midterm, endsem);
            Ui.msgSuccess(this, "Scores saved.");
            loadRoster(currentRosterSectionId);
        } catch (Exception e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void saveAll() {
        if (currentRosterSectionId == -1) {
            Ui.msgError(this, "No roster loaded.");
            return;
        }
        try {
            int rows = tblRoster.getRowCount();
            for (int r = 0; r < rows; r++) {
                int lastCol = tblRoster.getColumnCount() - 1;
                Object eidObj = tblRoster.getValueAt(r, lastCol);
                if (eidObj == null)
                    continue;
                int enrollmentId = (int) eidObj;
                Double quiz = parseDoubleOrNull(tblRoster.getValueAt(r, 3));
                Double midterm = parseDoubleOrNull(tblRoster.getValueAt(r, 4));
                Double endsem = parseDoubleOrNull(tblRoster.getValueAt(r, 5));
                try {
                    instructor.saveScores(session, currentRosterSectionId, enrollmentId, quiz, midterm, endsem);
                } catch (Exception ex) {
                    System.err.println("Failed to save enrollment " + enrollmentId + ": " + ex.getMessage());
                }
            }
            Ui.msgSuccess(this, "All scores processed (check console for per-row errors).");
            loadRoster(currentRosterSectionId);
        } catch (Exception e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private Double parseDoubleOrNull(Object o) {
        if (o == null)
            return null;
        String s = o.toString().trim();
        if (s.isEmpty())
            return null;
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void loadStats(int sectionId) {
        try {
            tblStats.setModel(instructor.classStats(session, sectionId));
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void exportGrades(int sectionId) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("grades_section_" + sectionId + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                instructor.exportGradesCsv(session, sectionId, fc.getSelectedFile().getAbsolutePath());
                Ui.msgSuccess(this, "Grades exported to: " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        }
    }

    private void importGrades(int sectionId) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fc.getSelectedFile().getAbsolutePath();
                java.util.List<String[]> preview = instructor.previewGradesCsv(session, sectionId, path);
                String[] cols = { "Roll", "Username", "Student ID", "Enrolled", "Quiz", "Midterm", "EndSem",
                        "Error" };
                javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                };
                for (String[] r : preview)
                    m.addRow(r);

                JTable tbl = new JTable(m);
                JScrollPane sp = new JScrollPane(tbl);
                sp.setPreferredSize(new Dimension(800, 300));
                int confirm = JOptionPane.showConfirmDialog(this, sp, "Preview Import - Confirm to proceed",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (confirm == JOptionPane.OK_OPTION) {
                    java.util.List<String> res = instructor.importGradesCsv(session, sectionId, path);
                    StringBuilder sb = new StringBuilder();
                    int ok = 0;
                    for (String r : res) {
                        sb.append(r).append("\n");
                        if (r.contains("OK"))
                            ok++;
                    }
                    JTextArea ta = new JTextArea(sb.toString());
                    ta.setEditable(false);
                    JScrollPane rsp = new JScrollPane(ta);
                    rsp.setPreferredSize(new Dimension(600, 300));
                    JOptionPane.showMessageDialog(this, rsp, "Import Results (" + ok + "/" + res.size() + ")",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRoster(sectionId);
                    loadStats(sectionId);
                }
            } catch (Exception ex) {
                Ui.msgError(this, ex.getMessage());
            }
        }
    }

    private void setWeights(int sectionId) {
        try {
            double[] w = instructor.getWeights(sectionId);
            int q = (int) Math.round(w[0] * 100);
            int m = (int) Math.round(w[1] * 100);
            int e = (int) Math.round(w[2] * 100);

            JPanel p = new JPanel(new GridLayout(3, 2, 6, 6));
            p.add(new JLabel("Quiz %:"));
            JTextField fq = new JTextField(String.valueOf(q));
            p.add(fq);
            p.add(new JLabel("Midterm %:"));
            JTextField fm = new JTextField(String.valueOf(m));
            p.add(fm);
            p.add(new JLabel("EndSem %:"));
            JTextField fe = new JTextField(String.valueOf(e));
            p.add(fe);

            int res = JOptionPane.showConfirmDialog(this, p, "Set Weights (percent)", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                int nq = Integer.parseInt(fq.getText().trim());
                int nm = Integer.parseInt(fm.getText().trim());
                int ne = Integer.parseInt(fe.getText().trim());
                instructor.setWeights(session, sectionId, nq, nm, ne);
                Ui.msgSuccess(this, "Weights saved and finals recomputed.");
                loadRoster(sectionId);
                loadStats(sectionId);
            }
        } catch (Exception ex) {
            Ui.msgError(this, ex.getMessage());
        }
    }

    private JPanel settingsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 0, 20));
        JLabel title = Ui.createLabelBold("Settings");
        JLabel subtitle = Ui.createLabel("Account and preferences");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        subtitle.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Ui.BG_LIGHT);
        JPanel box = new JPanel(new GridLayout(3, 1, 8, 8));
        box.setBorder(new EmptyBorder(12, 12, 12, 12));
        JButton btnChange = Ui.button("Change Password", this::showChangePasswordDialog);
        box.add(btnChange);
        box.add(Ui.createLabel("Other preferences will appear here."));
        center.add(box);

        p.add(header, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }
}