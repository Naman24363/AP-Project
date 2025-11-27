package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.ui.auth.LoginFrame;
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
    private final JTabbedPane tabs;
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

        // Top header similar to student dashboard
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        JLabel lblWelcome = new JLabel("Welcome, " + s.username);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(Color.WHITE);
        left.add(lblWelcome);
        JLabel lblSub = new JLabel("Admin Dashboard");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 230, 250));
        left.add(lblSub);

        // Logout button on right
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
        tabs.add("Dashboard", adminLandingPanel());
        tabs.add("Maintenance", maintenancePanel());
        tabs.add("Users", usersPanel());
        tabs.add("Courses", coursesPanel());
        tabs.add("Sections", sectionsPanel());
        tabs.add("Catalog", catalogPanel());

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);
        add(main);
        // Ensure sections list is populated on startup
        refreshSections();
    }

    private JPanel adminLandingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header similar to Student dashboard: title + short description
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 0, 20));
        JLabel title = Ui.createLabelBold("Admin Dashboard");
        JLabel subtitle = Ui.createLabel("Administrative quick actions and management");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        subtitle.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        p.add(header, BorderLayout.NORTH);

        java.util.function.Consumer<String> openTab = (name) -> {
            // select the tab in the dashboard's tab pane by title
            if (AdminDashboard.this.tabs == null)
                return;
            for (int i = 0; i < AdminDashboard.this.tabs.getTabCount(); i++) {
                if (AdminDashboard.this.tabs.getTitleAt(i).equalsIgnoreCase(name)) {
                    AdminDashboard.this.tabs.setSelectedIndex(i);
                    return;
                }
            }
        };

        // Admin-focused tiles (Create/Edit Users, Create/Edit Courses, Create
        // Sections, Maintenance, Catalog, Settings) - make them smaller and
        // contained in a white card for a cleaner look
        JButton bUsers = Ui.tileButton("Create/Edit Users", () -> openTab.accept("Users"));
        JButton bCourses = Ui.tileButton("Create/Edit Courses", () -> openTab.accept("Courses"));
        JButton bSections = Ui.tileButton("Create Sections", () -> openTab.accept("Sections"));
        JButton bMaint = Ui.tileButton("Maintenance", () -> openTab.accept("Maintenance"));
        JButton bCatalog = Ui.tileButton("Catalog", () -> openTab.accept("Catalog"));
        JButton bSettings = Ui.tileButton("Settings", this::showChangePasswordDialog);

        // reduce size for admin landing compact layout
        Dimension smallTile = new Dimension(140, 84);
        bUsers.setPreferredSize(smallTile);
        bCourses.setPreferredSize(smallTile);
        bSections.setPreferredSize(smallTile);
        bMaint.setPreferredSize(smallTile);
        bCatalog.setPreferredSize(smallTile);
        bSettings.setPreferredSize(smallTile);

        grid.add(bUsers);
        grid.add(bCourses);
        grid.add(bSections);
        grid.add(bMaint);
        grid.add(bCatalog);
        grid.add(bSettings);

        // Put tiles inside a white card with padding and a subtle border
        JPanel card = Ui.createPanel(new BorderLayout(), Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
                new EmptyBorder(18, 18, 18, 18)));
        card.add(grid, BorderLayout.CENTER);

        // Center the card and add slight margin from header
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(Ui.BG_LIGHT);
        centerWrap.add(card);

        p.add(centerWrap, BorderLayout.CENTER);

        return p;
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
            try {
                if (!admin.verifyCurrentPassword(session, oldP)) {
                    Ui.msgError(d, "Current password incorrect");
                    return;
                }
                // basic validation
                if (n.length() < 8) {
                    Ui.msgError(d, "Password must be at least 8 characters");
                    return;
                }
                admin.changePassword(session, n);
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

        // Year removed from admin user creation form (use default year when needed)

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
                    int year = 1; // default year
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
        // NOTE: bulk Delete Sections button removed from Courses panel; individual
        // section deletion is available in the Sections panel for safety and direct
        // selection.
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

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Ui.BG_LIGHT);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel leftFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftFields.setOpaque(false);

        JLabel lbl = Ui.createLabelBold("Create New Section");
        JLabel lbl1 = Ui.createLabel("Course ID:");
        // use shared combo so it can be refreshed when courses change
        JComboBox<String> cbCourse = cbCourseForSection;
        JLabel lbl2 = Ui.createLabel("Instructor ID:");
        JComboBox<String> cbInst = cbInstForSection;
        JLabel lbl3 = Ui.createLabel("Day/Time:");
        JTextField txtDay = Ui.createTextField(15);
        JLabel lblSemester = Ui.createLabel("Semester:");
        JComboBox<String> cbSemester = new JComboBox<>(new String[] { "Summer", "Winter", "Monsoon" });
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
                    // Resolve courseId from fresh course list to avoid stale map
                    Integer courseId = null;
                    try {
                        java.util.List<edu.univ.erp.domain.Course> fresh = catalog.getAllCourses();
                        for (edu.univ.erp.domain.Course c : fresh) {
                            if (c.code.equals(selCode)) {
                                courseId = c.courseId;
                                break;
                            }
                        }
                    } catch (SQLException se) {
                        Ui.msgError(this, "Failed to load courses: " + se.getMessage());
                        return;
                    }
                    if (courseId == null) {
                        Ui.msgError(this, "Selected course not found");
                        return;
                    }

                    // Parse instructor id from label like "3 - inst1" (shared combo uses this
                    // format)
                    Integer instId = null;
                    try {
                        if (selInstLabel != null && selInstLabel.contains(" - ")) {
                            instId = Integer.parseInt(selInstLabel.split(" - ")[0]);
                        }
                    } catch (NumberFormatException nfe) {
                        Ui.msgError(this, "Invalid instructor selected");
                        return;
                    }
                    String day = txtDay.getText().trim();
                    String semester = (String) cbSemester.getSelectedItem();
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
                    admin.createSection(session, courseId, instId, day, room, cap, semester, 2025);
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

        leftFields.add(lbl);
        leftFields.add(Box.createHorizontalStrut(15));
        leftFields.add(lbl1);
        leftFields.add(cbCourse);
        leftFields.add(lbl2);
        leftFields.add(cbInst);
        leftFields.add(lbl3);
        leftFields.add(txtDay);
        leftFields.add(lblSemester);
        leftFields.add(cbSemester);
        leftFields.add(lbl4);
        leftFields.add(txtRoom);
        leftFields.add(lbl5);
        leftFields.add(spnCap);
        // Group action buttons into a right-aligned actions panel so they are
        // fully visible and don't get truncated by wrapping in the input flow.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnCreateHolder[0]);

        // Add a Delete Selected Section button so admin can remove a specific
        // section shown in the sections table (select row then click delete)
        JButton btnDeleteSelected = Ui.buttonSecondary("Delete Selected Section", () -> {
            int row = tblSections.getSelectedRow();
            if (row == -1) {
                Ui.msgError(this, "Please select a section row to delete.");
                return;
            }
            Object idObj = tblSections.getValueAt(row, 0);
            if (idObj == null || idObj.toString().trim().isEmpty()) {
                Ui.msgError(this, "Selected row is not a section.");
                return;
            }
            int sectionId;
            try {
                sectionId = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid section id selected.");
                return;
            }
            int r = JOptionPane.showConfirmDialog(this, "Delete section " + sectionId + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION)
                return;
            try {
                admin.deleteSection(session, sectionId);
                Ui.msgSuccess(this, "Section deleted.");
                refreshSections();
                try {
                    refreshCatalog();
                } catch (Exception ignored) {
                }
                try {
                    refreshCourseDropdowns();
                } catch (Exception ignored) {
                }
            } catch (Exception ex) {
                Ui.msgError(this, ex.getMessage());
            }
        });
        actions.add(btnDeleteSelected);
        JButton btnRemoveEnrolls = Ui.buttonSecondary("Remove Enrollments", () -> {
            int row = tblSections.getSelectedRow();
            if (row == -1) {
                Ui.msgError(this, "Please select a section row to clear enrollments.");
                return;
            }
            Object idObj = tblSections.getValueAt(row, 0);
            if (idObj == null || idObj.toString().trim().isEmpty()) {
                Ui.msgError(this, "Selected row is not a section.");
                return;
            }
            int sectionId;
            try {
                sectionId = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException nfe) {
                Ui.msgError(this, "Invalid section id selected.");
                return;
            }
            int r = JOptionPane.showConfirmDialog(this,
                    "Remove ALL students from section " + sectionId + "? This will delete enrollments and grades.",
                    "Confirm Remove Enrollments", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION)
                return;
            try {
                int removed = admin.removeEnrollmentsForSection(session, sectionId);
                Ui.msgSuccess(this, "Removed " + removed + " enrollment(s) from section.");
                refreshSections();
                try {
                    refreshCatalog();
                } catch (Exception ignored) {
                }
                try {
                    refreshCourseDropdowns();
                } catch (Exception ignored) {
                }
            } catch (Exception ex) {
                Ui.msgError(this, ex.getMessage());
            }
        });
        actions.add(btnRemoveEnrolls);
        inputPanel.add(leftFields, BorderLayout.CENTER);
        inputPanel.add(actions, BorderLayout.EAST);

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
            javax.swing.table.DefaultTableModel full = catalog.listCatalog();
            // Filter out rows that represent courses without sections (Section ID empty)
            int cols = full.getColumnCount();
            String[] colNames = new String[cols];
            for (int i = 0; i < cols; i++)
                colNames[i] = full.getColumnName(i);
            javax.swing.table.DefaultTableModel filtered = new javax.swing.table.DefaultTableModel(colNames, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (int r = 0; r < full.getRowCount(); r++) {
                Object idObj = full.getValueAt(r, 0);
                if (idObj == null)
                    continue;
                String s = idObj.toString().trim();
                if (s.isEmpty())
                    continue; // skip course-only row
                Object[] row = new Object[cols];
                for (int c = 0; c < cols; c++)
                    row[c] = full.getValueAt(r, c);
                filtered.addRow(row);
            }
            tblSections.setModel(filtered);
        } catch (SQLException e) {
            Ui.msgError(this, e.getMessage());
        }
    }
}