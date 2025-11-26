package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class InstructorDashboard extends JFrame {
    private final Session session;
    private final InstructorService instructor = new InstructorService();
    private final JTable tblSections = new JTable();
    private final JTable tblRoster = new JTable();
    private final JTable tblStats = new JTable();

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
        top.add(btnLoad);
        top.add(btnExport);
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
            tblRoster.setModel(instructor.roster(sectionId));
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void loadStats(int sectionId) {
        try {
            tblStats.setModel(instructor.classStats(sectionId));
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }

    private void exportGrades(int sectionId) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("grades_section_" + sectionId + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                instructor.exportGradesCsv(sectionId, fc.getSelectedFile().getAbsolutePath());
                Ui.msgSuccess(this, "Grades exported to: " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        }
    }
}