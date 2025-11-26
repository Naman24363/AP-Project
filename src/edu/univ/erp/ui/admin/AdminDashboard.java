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
    // shared UI components for live refresh
    private final JComboBox<String> cbInstForSection = new JComboBox<>();
    private final JComboBox<String> cbDeleteInstructor = new JComboBox<>();
    private final JTable tblSections = new JTable();
    // shared combo boxes so they can be refreshed when courses change
    private final JComboBox<String> cbDeleteCourse = new JComboBox<>();
    private final JComboBox<String> cbCourseForSection = new JComboBox<>();

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

    private void showAddCourseDialog() {
        JDialog d = new JDialog(this, "Add New Course", true);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        p.add(Ui.createLabelBold("New Course Details"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("Course Code"), gbc);
        gbc.gridx = 1;
        JTextField txtCode = Ui.createTextField(20);
        p.add(txtCode, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("Course Title"), gbc);
        gbc.gridx = 1;
        JTextField txtTitle = Ui.createTextField(30);
        p.add(txtTitle, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(Ui.createLabel("Credits"), gbc);
        gbc.gridx = 1;
        JSpinner spnCredits = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        p.add(spnCredits, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        JButton btnAdd = Ui.button("Add Course", () -> {
            try {
                String code = txtCode.getText().trim();
                String title = txtTitle.getText().trim();
                int credits = (int) spnCredits.getValue();
                if (code.isEmpty() || title.isEmpty()) {
                    Ui.msgError(d, "All fields required");
                    return;
                }
                admin.createCourse(session, code, title, credits);
                Ui.msgSuccess(d, "Course created successfully");
                d.dispose();
                refreshCatalog();
                try {
                    refreshCourseDropdowns();
                } catch (SQLException ignored) {
                }
                // also refresh sections table so new course shows up immediately
                refreshSections();
            } catch (Exception ex) {
                Ui.msgError(d, ex.getMessage());
            }
        });
        JButton btnCancel = Ui.buttonSecondary("Cancel", () -> d.dispose());
        btns.add(btnAdd);
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
        JPanel p = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = Ui.createLabelBold("New User Details");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        form.add(Ui.createLabel("Role:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbRole = new JComboBox<>(new String[] { "STUDENT", "INSTRUCTOR", "ADMIN" });
        form.add(cbRole, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = Ui.createTextField(15);
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPass = Ui.createPasswordField(15);
        form.add(txtPass, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Roll Number:"), gbc);
        gbc.gridx = 1;
        JTextField txtRoll = Ui.createTextField(15);
        form.add(txtRoll, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Program/Course:"), gbc);
        gbc.gridx = 1;
        JTextField txtProgram = Ui.createTextField(15);
        form.add(txtProgram, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Year:"), gbc);
        gbc.gridx = 1;
        JSpinner spnYear = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        form.add(spnYear, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Department/Branch:"), gbc);
        gbc.gridx = 1;
        JTextField txtDept = Ui.createTextField(15);
        form.add(txtDept, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField txtPhone = Ui.createTextField(15);
        form.add(txtPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Email ID:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = Ui.createTextField(20);
        form.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(Ui.createLabel("Aadhar No:"), gbc);
        gbc.gridx = 1;
        JTextField txtAadhar = Ui.createTextField(20);
        form.add(txtAadhar, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnCreate = Ui.button("Submit", () -> {
            try {
                String role = (String) cbRole.getSelectedItem();
                String username = txtUsername.getText().trim();
                String password = new String(txtPass.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    Ui.msgError(this, "Username and password required");
                    return;
                }

                int newId = admin.nextUserId();
                admin.createUser(session, newId, username, role, password);

                if ("STUDENT".equals(role)) {
                    String roll = txtRoll.getText().trim();
                    String prog = txtProgram.getText().trim();
                    int year = (int) spnYear.getValue();
                    if (roll.isEmpty() || prog.isEmpty()) {
                        Ui.msgError(this, "Roll and program required for student");
                        return;
                    }
                    admin.createStudentProfile(session, newId, roll, prog, year);
                } else if ("INSTRUCTOR".equals(role)) {
                    String dept = txtDept.getText().trim();
                    admin.createInstructorProfile(session, newId, dept);
                    // add to instructor dropdowns immediately so UI reflects new instructor
                    String instLabel = newId + " - " + username;
                    try {
                        cbDeleteInstructor.addItem(instLabel);
                    } catch (Exception ignored) {
                    }
                    try {
                        refreshInstructorDropdowns();
                    } catch (Exception ignored) {
                    }
                }

                Ui.msgSuccess(this, "User created with ID: " + newId);
                // clear form
                txtUsername.setText("");
                txtPass.setText("");
                txtRoll.setText("");
                txtProgram.setText("");
                txtDept.setText("");
                txtPhone.setText("");
                txtEmail.setText("");
                txtAadhar.setText("");
                spnYear.setValue(1);
            } catch (Exception e) {
                Ui.msgError(this, e.getMessage());
            }
        });
        // Delete instructor control
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(Ui.createLabel("Delete Instructor:"), gbc);
        gbc.gridx = 1;
        // use shared delete instructor combo (so it updates live)
        try {
            java.util.Map<Integer, String> inst = admin.getAllInstructors();
            for (java.util.Map.Entry<Integer, String> e : inst.entrySet())
                cbDeleteInstructor.addItem(e.getKey() + " - " + e.getValue());
        } catch (Exception ignored) {
        }
        form.add(cbDeleteInstructor, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JButton btnDeleteInst = Ui.buttonSecondary("Delete Instructor", () -> {
            String sel = (String) cbDeleteInstructor.getSelectedItem();
            if (sel == null) {
                Ui.msgError(this, "No instructor selected");
                return;
            }
            int id = Integer.parseInt(sel.split(" - ")[0]);
            int r = JOptionPane.showConfirmDialog(this, "Delete instructor " + sel + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    admin.deleteInstructor(session, id);
                    Ui.msgSuccess(this, "Instructor deleted.");
                    cbDeleteInstructor.removeItem(sel);
                    try {
                        refreshInstructorDropdowns();
                    } catch (Exception ignored) {
                    }
                    try {
                        refreshSections();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ex) {
                    Ui.msgError(this, ex.getMessage());
                }
            }
        });
        form.add(btnDeleteInst, gbc);

        // Unassign instructor from all sections (makes deletion possible)
        gbc.gridx = 1;
        JButton btnUnassignInst = Ui.buttonSecondary("Unassign Instructor", () -> {
            String sel = (String) cbDeleteInstructor.getSelectedItem();
            if (sel == null) {
                Ui.msgError(this, "No instructor selected");
                return;
            }
            int id = Integer.parseInt(sel.split(" - ")[0]);
            try {
                java.util.List<Integer> secs = admin.getSectionsForInstructor(id);
                if (secs.isEmpty()) {
                    Ui.msgInfo(this, "Instructor not assigned to any sections.");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (Integer s : secs)
                    sb.append(s).append(", ");
                String list = sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
                int r = JOptionPane.showConfirmDialog(this,
                        "Unassign instructor " + sel + " from sections: " + list + "?",
                        "Confirm Unassign", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    int changed = admin.unassignInstructorFromSections(session, id);
                    Ui.msgSuccess(this, "Unassigned instructor from " + changed + " section(s).");
                    try {
                        refreshInstructorDropdowns();
                    } catch (Exception ignored) {
                    }
                    try {
                        refreshSections();
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                Ui.msgError(this, ex.getMessage());
            }
        });
        form.add(btnUnassignInst, gbc);

        JButton btnCancel = Ui.buttonSecondary("Cancel", () -> {
            txtUsername.setText("");
            txtPass.setText("");
        });
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.add(btnCreate);
        btns.add(btnCancel);
        gbc.gridy++;
        form.add(btns, gbc);

        // Pre-fill roll with next user id if possible
        try {
            txtRoll.setText(String.valueOf(admin.nextUserId()));
        } catch (Exception ignored) {
        }

        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel coursesPanel() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        inputPanel.setBackground(Ui.BG_LIGHT);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lbl = Ui.createLabelBold("Courses");
        JButton btnCreateDialog = Ui.button("Add New Course", this::showAddCourseDialog);

        inputPanel.add(lbl);
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(btnCreateDialog);
        // Delete course controls (uses shared field combo so it can be refreshed)
        try {
            java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
            for (edu.univ.erp.domain.Course c : courses)
                cbDeleteCourse.addItem(c.code + " (id=" + c.courseId + ")");
        } catch (Exception ignored) {
        }
        JButton btnDeleteSections = Ui.buttonSecondary("Delete Sections", () -> {
            String sel = (String) cbDeleteCourse.getSelectedItem();
            if (sel == null) {
                Ui.msgError(this, "No course selected");
                return;
            }
            int id = Integer.parseInt(sel.substring(sel.indexOf("id=") + 3, sel.indexOf(")")));
            try {
                java.util.List<Integer> secs = admin.getSectionsForCourse(id);
                if (secs.isEmpty()) {
                    Ui.msgInfo(this, "This course has no sections.");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (Integer s : secs)
                    sb.append(s).append(", ");
                String list = sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
                int r = JOptionPane.showConfirmDialog(this,
                        "Delete all sections for course " + sel + "?\nSections: " + list,
                        "Confirm Delete Sections", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    int deleted = admin.deleteSectionsForCourse(session, id);
                    Ui.msgSuccess(this, "Deleted " + deleted + " section(s).");
                    refreshCatalog();
                    try {
                        refreshSections();
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                Ui.msgError(this, ex.getMessage());
            }
        });
        JButton btnDeleteCourse = Ui.buttonSecondary("Delete Course", () -> {
            String sel = (String) cbDeleteCourse.getSelectedItem();
            if (sel == null) {
                Ui.msgError(this, "No course selected");
                return;
            }
            int id = Integer.parseInt(sel.substring(sel.indexOf("id=") + 3, sel.indexOf(")")));
            int r = JOptionPane.showConfirmDialog(this, "Delete course " + sel + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    admin.deleteCourse(session, id);
                    Ui.msgSuccess(this, "Course deleted.");
                    cbDeleteCourse.removeItem(sel);
                    refreshCatalog();
                    try {
                        refreshCourseDropdowns();
                    } catch (Exception ignored) {
                    }
                    try {
                        refreshSections();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ex) {
                    Ui.msgError(this, ex.getMessage());
                }
            }
        });
        inputPanel.add(cbDeleteCourse);
        inputPanel.add(btnDeleteSections);
        inputPanel.add(btnDeleteCourse);

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
        // use shared combo so it can be refreshed when courses change
        JComboBox<String> cbCourse = cbCourseForSection;
        JLabel lbl2 = Ui.createLabel("Instructor ID:");
        JComboBox<String> cbInst = cbInstForSection;
        JLabel lbl3 = Ui.createLabel("Day/Time:");
        JTextField txtDay = Ui.createTextField(15);
        JLabel lbl4 = Ui.createLabel("Room:");
        JTextField txtRoom = Ui.createTextField(10);
        JLabel lbl5 = Ui.createLabel("Capacity:");
        JSpinner spnCap = new JSpinner(new SpinnerNumberModel(40, 10, 100, 1));

        // Populate course and instructor dropdowns
        JButton[] btnCreateHolder = new JButton[1];
        java.util.Map<String, Integer> codeToId = new java.util.HashMap<>();
        java.util.Map<String, Integer> instLabelToId = new java.util.HashMap<>();
        try {
            java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
            for (edu.univ.erp.domain.Course c : courses) {
                cbCourseForSection.addItem(c.code);
                codeToId.put(c.code, c.courseId);
            }

            java.util.Map<Integer, String> instr = admin.getAllInstructors();
            for (java.util.Map.Entry<Integer, String> e : instr.entrySet()) {
                String label = e.getKey() + " - " + e.getValue();
                cbInst.addItem(label);
                instLabelToId.put(label, e.getKey());
            }
            btnCreateHolder[0] = Ui.button("Create Section", () -> {
                try {
                    String selCode = (String) cbCourse.getSelectedItem();
                    String selInstLabel = (String) cbInst.getSelectedItem();
                    if (selCode == null || selInstLabel == null) {
                        Ui.msgError(this, "Select course and instructor");
                        return;
                    }
                    Integer courseId = codeToId.get(selCode);
                    Integer instId = instLabelToId.get(selInstLabel);
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
                    try {
                        refreshSections();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ex) {
                    Ui.msgError(this, ex.getMessage());
                }
            });
        } catch (Exception ex) {
            // ignore populate errors; still show panel
            System.err.println("Failed to populate course/inst dropdowns: " + ex.getMessage());
            btnCreateHolder[0] = Ui.button("Create Section",
                    () -> Ui.msgError(this, "Cannot create section: data loading failed"));
        }

        inputPanel.add(lbl);
        inputPanel.add(Box.createHorizontalStrut(15));
        inputPanel.add(lbl1);
        inputPanel.add(cbCourse);
        inputPanel.add(lbl2);
        inputPanel.add(cbInst);
        inputPanel.add(lbl3);
        inputPanel.add(txtDay);
        inputPanel.add(lbl4);
        inputPanel.add(txtRoom);
        inputPanel.add(lbl5);
        inputPanel.add(spnCap);
        inputPanel.add(btnCreateHolder[0]);

        p.add(inputPanel, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = Ui.button("Refresh", this::refreshCatalog);
        bottom.add(btnRefresh);
        p.add(bottom, BorderLayout.SOUTH);
        p.add(new JScrollPane(tblSections), BorderLayout.CENTER);

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

    private void refreshCourseDropdowns() throws SQLException {
        // refresh delete-dropdown
        cbDeleteCourse.removeAllItems();
        java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
        for (edu.univ.erp.domain.Course c : courses) {
            cbDeleteCourse.addItem(c.code + " (id=" + c.courseId + ")");
        }
        // refresh section course dropdown
        cbCourseForSection.removeAllItems();
        for (edu.univ.erp.domain.Course c : courses) {
            cbCourseForSection.addItem(c.code);
        }
    }

    private void refreshInstructorDropdowns() throws SQLException {
        cbInstForSection.removeAllItems();
        java.util.Map<Integer, String> instr = admin.getAllInstructors();
        for (java.util.Map.Entry<Integer, String> e : instr.entrySet()) {
            cbInstForSection.addItem(e.getKey() + " - " + e.getValue());
        }
    }

    private void refreshSections() {
        try {
            tblSections.setModel(catalog.listCatalog());
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }
}