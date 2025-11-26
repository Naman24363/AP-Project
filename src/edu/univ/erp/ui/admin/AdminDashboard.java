package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdminDashboard extends JFrame {
    private final Session session;
    private final AdminService admin = new AdminService();
    private final CatalogService catalog = new CatalogService();
    private final MaintenanceService maintenance = new MaintenanceService();
    private final JTable tblCatalog = new JTable();

    public AdminDashboard(Session s) {
        this.session = s;
        setTitle("Admin Dashboard - " + s.username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Maintenance", maintenancePanel());
        tabs.add("Users", usersPanel());
        tabs.add("Courses", coursesPanel());
        tabs.add("Sections", sectionsPanel());
        tabs.add("Catalog", catalogPanel());
        add(tabs);
    }

    private JPanel maintenancePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JLabel lbl = Ui.createLabelBold("System Maintenance Mode");
        JCheckBox chk = new JCheckBox("Enable Maintenance (Read-Only)");
        boolean maint = false;
        try {
            maint = MaintenanceService.isMaintenanceOn();
        } catch (Exception ignored) {
            maint = false;
        }
        chk.setSelected(maint);

        JButton btnApply = Ui.button("Apply", () -> {
            try {
                maintenance.setMaintenance(chk.isSelected());
                Ui.msgSuccess(this, "Maintenance mode updated");
            } catch (SQLException e) {
                Ui.msgError(this, e.getMessage());
            }
        });
        p.add(lbl);
        p.add(chk);
        p.add(btnApply);
        return p;
    }

    private JPanel usersPanel() {
        JPanel p = new JPanel();
        p.add(new JLabel("Create new users and assign roles"));
        return p;
    }

    private JPanel coursesPanel() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        inputPanel.setBackground(Ui.BG_LIGHT);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lbl = Ui.createLabelBold("Create New Course");
        JLabel lbl1 = Ui.createLabel("Code:");
        JTextField txtCode = Ui.createTextField(10);
        JLabel lbl2 = Ui.createLabel("Title:");
        JTextField txtTitle = Ui.createTextField(20);
        JLabel lbl3 = Ui.createLabel("Credits:");
        JSpinner spnCredits = new JSpinner(new SpinnerNumberModel(3, 1, 4, 1));

        JButton btnCreate = Ui.button("Create Course", () -> {
            try {
                String code = txtCode.getText().trim();
                String title = txtTitle.getText().trim();
                int credits = (int) spnCredits.getValue();
                if (code.isEmpty() || title.isEmpty()) {
                    Ui.msgError(this, "All fields required");
                    return;
                }
                admin.createCourse(session, code, title, credits);
                Ui.msgSuccess(this, "Course created successfully");
                txtCode.setText("");
                txtTitle.setText("");
                spnCredits.setValue(3);
                refreshCatalog();
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });

        inputPanel.add(lbl);
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(lbl1);
        inputPanel.add(txtCode);
        inputPanel.add(lbl2);
        inputPanel.add(txtTitle);
        inputPanel.add(lbl3);
        inputPanel.add(spnCredits);
        inputPanel.add(btnCreate);

        p.add(inputPanel, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = Ui.button("Refresh", this::refreshCatalog);
        bottom.add(btnRefresh);
        p.add(bottom, BorderLayout.SOUTH);
        p.add(new JScrollPane(tblCatalog), BorderLayout.CENTER);

        return p;
    }

    private JPanel sectionsPanel() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        inputPanel.setBackground(Ui.BG_LIGHT);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lbl = Ui.createLabelBold("Create New Section");
        JLabel lbl1 = Ui.createLabel("Course ID:");
        JSpinner spnCourse = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JLabel lbl2 = Ui.createLabel("Instructor ID:");
        JSpinner spnInst = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        JLabel lbl3 = Ui.createLabel("Day/Time:");
        JTextField txtDay = Ui.createTextField(15);
        JLabel lbl4 = Ui.createLabel("Room:");
        JTextField txtRoom = Ui.createTextField(10);
        JLabel lbl5 = Ui.createLabel("Capacity:");
        JSpinner spnCap = new JSpinner(new SpinnerNumberModel(40, 10, 100, 1));

        JButton btnCreate = Ui.button("Create Section", () -> {
            try {
                int courseId = (int) spnCourse.getValue();
                Integer instId = (Integer) spnInst.getValue();
                String day = txtDay.getText().trim();
                String room = txtRoom.getText().trim();
                int cap = (int) spnCap.getValue();
                if (day.isEmpty() || room.isEmpty()) {
                    Ui.msgError(this, "All fields required");
                    return;
                }
                if (cap < 10) {
                    Ui.msgError(this, "Capacity must be at least 10");
                    return;
                }
                admin.createSection(session, courseId, instId, day, room, cap, "Fall", 2025);
                Ui.msgSuccess(this, "Section created successfully");
                txtDay.setText("");
                txtRoom.setText("");
                spnCap.setValue(40);
                refreshCatalog();
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });

        inputPanel.add(lbl);
        inputPanel.add(Box.createHorizontalStrut(15));
        inputPanel.add(lbl1);
        inputPanel.add(spnCourse);
        inputPanel.add(lbl2);
        inputPanel.add(spnInst);
        inputPanel.add(lbl3);
        inputPanel.add(txtDay);
        inputPanel.add(lbl4);
        inputPanel.add(txtRoom);
        inputPanel.add(lbl5);
        inputPanel.add(spnCap);
        inputPanel.add(btnCreate);

        p.add(inputPanel, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = Ui.button("Refresh", this::refreshCatalog);
        bottom.add(btnRefresh);
        p.add(bottom, BorderLayout.SOUTH);
        p.add(new JScrollPane(tblCatalog), BorderLayout.CENTER);

        return p;
    }

    private JPanel catalogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = Ui.button("Refresh", this::refreshCatalog);
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tblCatalog), BorderLayout.CENTER);
        refreshCatalog();
        return p;
    }

    private void refreshCatalog() {
        try {
            tblCatalog.setModel(catalog.listCatalog());
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }
}