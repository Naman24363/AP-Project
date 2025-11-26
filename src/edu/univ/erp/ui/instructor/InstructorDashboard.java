package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InstructorDashboard extends JFrame {
    private final Session session;
    private final InstructorService instructor = new InstructorService();
    private final JTable tblSections = new JTable();
    private final JTable tblRoster = new JTable();
    private final JTable tblStats = new JTable();
    private int currentRosterSectionId = -1;

    public InstructorDashboard(Session s) {
        this.session = s;
        setTitle("Instructor Dashboard - " + s.username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("My Sections", sectionsPanel());
        tabs.add("Grades", gradesPanel());
        tabs.add("Class Stats", statsPanel());
        add(tabs);
        refreshSections();
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
        JSpinner spnSection = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        top.add(spnSection);
        JButton btnLoad = Ui.button("Load Roster", () -> loadRoster((int) spnSection.getValue()));
        JButton btnExport = Ui.button("Export CSV", () -> exportGrades((int) spnSection.getValue()));
        JButton btnImport = Ui.button("Import CSV", () -> importGrades((int) spnSection.getValue()));
        JButton btnSaveSelected = Ui.button("Save Selected", () -> saveSelected());
        JButton btnSaveAll = Ui.button("Save All", () -> saveAll());
        JButton btnSetWeights = Ui.button("Set Weights", () -> setWeights((int) spnSection.getValue()));
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
        JSpinner spnSection = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        top.add(spnSection);
        JButton btnLoad = Ui.button("Load Stats", () -> loadStats((int) spnSection.getValue()));
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
            // Load base (read-only) model from service and copy into an editable model
            DefaultTableModel base = instructor.roster(session, sectionId);
            int cols = base.getColumnCount();
            String[] colNames = new String[cols];
            for (int i = 0; i < cols; i++)
                colNames[i] = base.getColumnName(i);

            DefaultTableModel editable = new DefaultTableModel(colNames, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    // Allow editing of Quiz, Midterm, EndSem (assumed columns 3,4,5)
                    return c == 3 || c == 4 || c == 5;
                }
            };

            for (int r = 0; r < base.getRowCount(); r++) {
                Object[] row = new Object[cols];
                for (int c = 0; c < cols; c++)
                    row[c] = base.getValueAt(r, c);
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
            Object eidObj = tblRoster.getValueAt(row, 0);
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
                Object eidObj = tblRoster.getValueAt(r, 0);
                if (eidObj == null)
                    continue;
                int enrollmentId = (int) eidObj;
                Double quiz = parseDoubleOrNull(tblRoster.getValueAt(r, 3));
                Double midterm = parseDoubleOrNull(tblRoster.getValueAt(r, 4));
                Double endsem = parseDoubleOrNull(tblRoster.getValueAt(r, 5));
                try {
                    instructor.saveScores(session, currentRosterSectionId, enrollmentId, quiz, midterm, endsem);
                } catch (Exception ex) {
                    // continue saving others but log the error
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
                    // reload roster and stats
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
                // reload roster and stats
                loadRoster(sectionId);
                loadStats(sectionId);
            }
        } catch (Exception ex) {
            Ui.msgError(this, ex.getMessage());
        }
    }
}