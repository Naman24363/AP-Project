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
    private final JComboBox<String> cbInstForSection = new JComboBox<>();
    private final JComboBox<String> cbDeleteInstructor = new JComboBox<>();
    private final JTable tblSections = new JTable();
    private final JComboBox<String> cbDeleteCourse = new JComboBox<>();
    private final JComboBox<String> cbCourseForSection = new JComboBox<>();

    public AdminDashboard(Session s) {
        this.session = s;
        setTitle("Admin Dashboard - " + s.username);
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
        JLabel lblSub = new JLabel("Admin Dashboard");
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
        tabs.add("Dashboard", adminLandingPanel());
        tabs.add("Maintenance", maintenancePanel());
        tabs.add("Backup & Restore", backupRestorePanel());
        tabs.add("Users", usersPanel());
        tabs.add("Courses", coursesPanel());
        tabs.add("Sections", sectionsPanel());
        tabs.add("Catalog", catalogPanel());

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);
        add(main);
        refreshSections();
    }

    private JPanel adminLandingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));

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
            if (AdminDashboard.this.tabs == null)
                return;
            for (int i = 0; i < AdminDashboard.this.tabs.getTabCount(); i++) {
                if (AdminDashboard.this.tabs.getTitleAt(i).equalsIgnoreCase(name)) {
                    AdminDashboard.this.tabs.setSelectedIndex(i);
                    return;
                }
            }
        };

        JButton bUsers = Ui.tileButton("Create/Edit Users", () -> openTab.accept("Users"));
        JButton bCourses = Ui.tileButton("Create/Edit Courses", () -> openTab.accept("Courses"));
        JButton bSections = Ui.tileButton("Create Sections", () -> openTab.accept("Sections"));
        JButton bMaint = Ui.tileButton("Maintenance", () -> openTab.accept("Maintenance"));
        JButton bCatalog = Ui.tileButton("Catalog", () -> openTab.accept("Catalog"));
        JButton bBackup = Ui.tileButton("Backup & Restore", () -> openTab.accept("Backup & Restore"));
        JButton bSettings = Ui.tileButton("Settings", this::showChangePasswordDialog);

        Dimension smallTile = new Dimension(140, 84);
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

        JPanel card = Ui.createPanel(new BorderLayout(), Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
                new EmptyBorder(18, 18, 18, 18)));
        card.add(grid, BorderLayout.CENTER);

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
        try {
            chk.setSelected(MaintenanceService.isMaintenanceOn());
        } catch (Exception ignored) {
        }

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

    private JPanel backupRestorePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = Ui.createLabelBold("Database Backup & Restore");
        JLabel subtitle = Ui.createLabel("Create backups and restore from existing backup files");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        subtitle.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Backup section
        JPanel backupPanel = Ui.createPanel(new GridBagLayout(), Color.WHITE);
        backupPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Create Backup"),
                new EmptyBorder(12, 12, 12, 12)));

        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.insets = new Insets(8, 8, 8, 8);
        bgbc.anchor = GridBagConstraints.WEST;
        bgbc.fill = GridBagConstraints.HORIZONTAL;

        bgbc.gridx = 0;
        bgbc.gridy = 0;
        JLabel lblBackupDir = Ui.createLabel("Backup Directory:");
        backupPanel.add(lblBackupDir, bgbc);

        bgbc.gridx = 1;
        JTextField txtBackupDir = Ui.createTextField(30);
        txtBackupDir.setText("backups");
        backupPanel.add(txtBackupDir, bgbc);

        bgbc.gridx = 2;
        JButton btnBrowseBackup = Ui.button("Browse", () -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtBackupDir.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        backupPanel.add(btnBrowseBackup, bgbc);

        bgbc.gridx = 0;
        bgbc.gridy = 1;
        bgbc.gridwidth = 3;
        JTextArea taBackupStatus = new JTextArea(4, 40);
        taBackupStatus.setEditable(false);
        taBackupStatus.setLineWrap(true);
        taBackupStatus.setWrapStyleWord(true);
        JScrollPane spBackup = new JScrollPane(taBackupStatus);
        backupPanel.add(spBackup, bgbc);

        bgbc.gridy = 2;
        bgbc.gridwidth = 1;
        bgbc.gridx = 0;
        JButton btnCreateBackup = Ui.button("Create Backup", () -> {
            String backupDir = txtBackupDir.getText().trim();
            if (backupDir.isEmpty()) {
                Ui.msgError(this, "Please specify a backup directory");
                return;
            }

            taBackupStatus.setText("Creating backup...\nPlease wait...");
            new Thread(() -> {
                try {
                    String backupPath = edu.univ.erp.service.BackupService.backupDatabase(session, backupDir);
                    taBackupStatus.setText("✓ Backup completed successfully!\n\nBackup file:\n" + backupPath);
                    Ui.msgSuccess(AdminDashboard.this,
                            "Database backup created successfully!\n\nFile: " + backupPath);
                } catch (Exception ex) {
                    taBackupStatus.setText("✗ Backup failed:\n" + ex.getMessage());
                    Ui.msgError(AdminDashboard.this, "Backup failed: " + ex.getMessage());
                }
            }).start();
        });
        backupPanel.add(btnCreateBackup, bgbc);

        center.add(backupPanel, gbc);

        // Restore section
        gbc.gridy = 1;
        JPanel restorePanel = Ui.createPanel(new GridBagLayout(), Color.WHITE);
        restorePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Restore from Backup"),
                new EmptyBorder(12, 12, 12, 12)));

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.insets = new Insets(8, 8, 8, 8);
        rgbc.anchor = GridBagConstraints.WEST;
        rgbc.fill = GridBagConstraints.HORIZONTAL;

        rgbc.gridx = 0;
        rgbc.gridy = 0;
        JLabel lblBackupFile = Ui.createLabel("Backup File:");
        restorePanel.add(lblBackupFile, rgbc);

        rgbc.gridx = 1;
        JTextField txtBackupFile = Ui.createTextField(30);
        restorePanel.add(txtBackupFile, rgbc);

        rgbc.gridx = 2;
        JButton btnBrowseRestore = Ui.button("Browse", () -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtBackupFile.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        restorePanel.add(btnBrowseRestore, rgbc);

        rgbc.gridx = 0;
        rgbc.gridy = 1;
        rgbc.gridwidth = 3;
        JTextArea taRestoreStatus = new JTextArea(4, 40);
        taRestoreStatus.setEditable(false);
        taRestoreStatus.setLineWrap(true);
        taRestoreStatus.setWrapStyleWord(true);
        JScrollPane spRestore = new JScrollPane(taRestoreStatus);
        restorePanel.add(spRestore, rgbc);

        rgbc.gridy = 2;
        rgbc.gridwidth = 1;
        rgbc.gridx = 0;
        JButton btnRestore = Ui.button("Restore Database", () -> {
            String backupFile = txtBackupFile.getText().trim();
            if (backupFile.isEmpty()) {
                Ui.msgError(this, "Please select a backup file");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "WARNING: This will replace all data in the database with the backup content.\n\n"
                            + "This action cannot be undone. Continue?",
                    "Confirm Database Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            taRestoreStatus.setText("Restoring database...\nPlease wait...");
            new Thread(() -> {
                try {
                    edu.univ.erp.service.BackupService.restoreDatabase(session, backupFile);
                    taRestoreStatus.setText(
                            "✓ Database restored successfully from backup!\n\nFile: " + backupFile);
                    Ui.msgSuccess(AdminDashboard.this,
                            "Database restored successfully!\n\nThe application will need to restart.");
                } catch (Exception ex) {
                    taRestoreStatus.setText("✗ Restore failed:\n" + ex.getMessage());
                    Ui.msgError(AdminDashboard.this, "Restore failed: " + ex.getMessage());
                }
            }).start();
        });
        restorePanel.add(btnRestore, rgbc);

        center.add(restorePanel, gbc);

        p.add(header, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);

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
                    int year = 1;
                    if (roll.isEmpty() || prog.isEmpty()) {
                        Ui.msgError(this, "Roll and program required for student");
                        return;
                    }
                    admin.createStudentProfile(session, newId, roll, prog, year);
                } else if ("INSTRUCTOR".equals(role)) {
                    String dept = txtDept.getText().trim();
                    admin.createInstructorProfile(session, newId, dept);
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
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(Ui.createLabel("Delete Instructor:"), gbc);
        gbc.gridx = 1;
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
        try {
            java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
            for (edu.univ.erp.domain.Course c : courses)
                cbDeleteCourse.addItem(c.code + " (id=" + c.courseId + ")");
        } catch (Exception ignored) {
        }
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

        JButton[] btnCreateHolder = new JButton[1];
        try {
            java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
            for (edu.univ.erp.domain.Course c : courses) {
                cbCourseForSection.addItem(c.code);
            }

            java.util.Map<Integer, String> instr = admin.getAllInstructors();
            for (java.util.Map.Entry<Integer, String> e : instr.entrySet()) {
                String label = e.getKey() + " - " + e.getValue();
                cbInst.addItem(label);
            }
            btnCreateHolder[0] = Ui.button("Create Section", () -> {
                try {
                    String selCode = (String) cbCourse.getSelectedItem();
                    String selInstLabel = (String) cbInst.getSelectedItem();
                    if (selCode == null || selInstLabel == null) {
                        Ui.msgError(this, "Select course and instructor");
                        return;
                    }
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

                    Integer instId = null;
                    try {
                        if (selInstLabel.contains(" - ")) {
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
                    // Validate Day/Time format: DayName HH:MM-HH:MM AM/PM (e.g., Monday 10:00-11:00
                    // AM)
                    String dayTimePattern = "(?i)^(Monday|Mon|Tuesday|Tue|Wednesday|Wed|Thursday|Thu|Friday|Fri|Saturday|Sat|Sunday|Sun)\\s+(1[0-2]|0?[1-9]):([0-5][0-9])-(1[0-2]|0?[1-9]):([0-5][0-9])\\s+(AM|PM)$";
                    if (!day.matches(dayTimePattern)) {
                        Ui.msgError(this,
                                "Invalid Day/Time format.\n\nExpected format: DayName HH:MM-HH:MM AM/PM\nExample: Monday 10:00-11:00 AM or Mon 10:00-11:00 AM\n\nNote: Day names (Mon/Tue/Wed/Thu/Fri/Sat/Sun or full names) are case-insensitive.");
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
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnCreateHolder[0]);

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
        cbDeleteCourse.removeAllItems();
        java.util.List<edu.univ.erp.domain.Course> courses = catalog.getAllCourses();
        for (edu.univ.erp.domain.Course c : courses) {
            cbDeleteCourse.addItem(c.code + " (id=" + c.courseId + ")");
        }
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
                    continue;
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