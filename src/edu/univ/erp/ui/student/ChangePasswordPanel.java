package edu.univ.erp.ui.student;

import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.AuthDb;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;

public class ChangePasswordPanel extends JPanel {
    private final Session session;
    private final JPasswordField pfOldPassword;
    private final JPasswordField pfNewPassword;
    private final JPasswordField pfConfirmPassword;
    private final JLabel lblStrength;

    public ChangePasswordPanel(Session s) {
        this.session = s;
        setLayout(new BorderLayout());
        setBackground(Ui.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.DIVIDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Title
        JLabel titleLabel = Ui.createLabelBold("Change Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Ui.PRIMARY);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = Ui.createLabel("Enter your old password and set a new one");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Ui.TEXT_LIGHT);
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Old Password
        contentPanel.add(createFormGroup(
                "Current Password",
                pfOldPassword = Ui.createPasswordField(25)));

        contentPanel.add(Box.createVerticalStrut(12));

        // New Password
        contentPanel.add(createFormGroup(
                "New Password",
                pfNewPassword = Ui.createPasswordField(25)));

        contentPanel.add(Box.createVerticalStrut(8));

        // Password strength indicator
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        strengthPanel.setOpaque(false);
        lblStrength = Ui.createLabel("Password Strength: Weak");
        lblStrength.setForeground(Ui.ERROR);
        strengthPanel.add(lblStrength);

        pfNewPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePasswordStrength();
            }
        });

        contentPanel.add(strengthPanel);
        contentPanel.add(Box.createVerticalStrut(12));

        // Confirm Password
        contentPanel.add(createFormGroup(
                "Confirm New Password",
                pfConfirmPassword = Ui.createPasswordField(25)));

        contentPanel.add(Box.createVerticalStrut(20));

        // Requirements
        JPanel reqPanel = new JPanel();
        reqPanel.setOpaque(false);
        reqPanel.setLayout(new BoxLayout(reqPanel, BoxLayout.Y_AXIS));

        JLabel reqTitle = Ui.createLabelBold("Password Requirements:");
        reqTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        reqPanel.add(reqTitle);

        String[] requirements = {
                "• At least 8 characters long",
                "• Mix of uppercase and lowercase letters",
                "• At least one number",
                "• At least one special character (!@#$%^&*)"
        };

        for (String req : requirements) {
            JLabel reqLabel = Ui.createLabel(req);
            reqLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            reqLabel.setForeground(Ui.TEXT_LIGHT);
            reqPanel.add(reqLabel);
        }

        contentPanel.add(reqPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnChange = Ui.button("Change Password", this::changePassword);
        JButton btnDashboard = Ui.buttonSecondary("Go to Dashboard", () -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.dispose();
                new StudentHome(session).setVisible(true);
            }
        });
        JButton btnCancel = Ui.buttonSecondary("Cancel", () -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.dispose();
                new StudentHome(session).setVisible(true);
            }
        });

        buttonPanel.add(btnChange);
        buttonPanel.add(btnDashboard);
        buttonPanel.add(btnCancel);
        contentPanel.add(buttonPanel);

        contentPanel.add(Box.createVerticalGlue());

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    private JPanel createFormGroup(String label, JComponent field) {
        JPanel group = new JPanel(new BorderLayout());
        group.setOpaque(false);

        JLabel lbl = Ui.createLabelBold(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        group.add(lbl, BorderLayout.NORTH);
        group.add(Box.createVerticalStrut(5), BorderLayout.WEST);
        group.add(field, BorderLayout.CENTER);

        return group;
    }

    private void updatePasswordStrength() {
        String password = new String(pfNewPassword.getPassword());
        PasswordStrength strength = calculateStrength(password);

        switch (strength) {
            case WEAK:
                lblStrength.setText("Password Strength: Weak");
                lblStrength.setForeground(Ui.ERROR);
                break;
            case MEDIUM:
                lblStrength.setText("Password Strength: Medium");
                lblStrength.setForeground(Ui.WARNING);
                break;
            case STRONG:
                lblStrength.setText("Password Strength: Strong");
                lblStrength.setForeground(Ui.SUCCESS);
                break;
        }
    }

    private PasswordStrength calculateStrength(String password) {
        if (password.length() < 8)
            return PasswordStrength.WEAK;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        int strength = 0;
        if (hasUpper)
            strength++;
        if (hasLower)
            strength++;
        if (hasDigit)
            strength++;
        if (hasSpecial)
            strength++;

        if (strength >= 3)
            return PasswordStrength.STRONG;
        if (strength >= 2)
            return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    private void changePassword() {
        String oldPassword = new String(pfOldPassword.getPassword());
        String newPassword = new String(pfNewPassword.getPassword());
        String confirmPassword = new String(pfConfirmPassword.getPassword());

        // Validation
        if (oldPassword.isEmpty()) {
            Ui.msgError(this, "Please enter your current password.");
            return;
        }

        if (newPassword.isEmpty()) {
            Ui.msgError(this, "Please enter a new password.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Ui.msgError(this, "New passwords do not match.");
            return;
        }

        if (newPassword.length() < 8) {
            Ui.msgError(this, "Password must be at least 8 characters long.");
            return;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            Ui.msgError(this, "Password must contain at least one uppercase letter.");
            return;
        }

        if (!newPassword.matches(".*[a-z].*")) {
            Ui.msgError(this, "Password must contain at least one lowercase letter.");
            return;
        }

        if (!newPassword.matches(".*\\d.*")) {
            Ui.msgError(this, "Password must contain at least one number.");
            return;
        }

        if (!newPassword.matches(".*[!@#$%^&*].*")) {
            Ui.msgError(this, "Password must contain at least one special character (!@#$%^&*).");
            return;
        }

        if (oldPassword.equals(newPassword)) {
            Ui.msgError(this, "New password must be different from current password.");
            return;
        }

        try {
            // Verify old password
            if (!verifyOldPassword(oldPassword)) {
                Ui.msgError(this, "Current password is incorrect.");
                return;
            }

            // Update password in database
            updatePasswordInDatabase(newPassword);
            Ui.msgSuccess(this, "Password changed successfully!");

            // Clear fields
            pfOldPassword.setText("");
            pfNewPassword.setText("");
            pfConfirmPassword.setText("");

            // Go back to home
            Timer timer = new Timer(1500, e -> {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.dispose();
                    new StudentHome(session).setVisible(true);
                }
            });
            timer.setRepeats(false);
            timer.start();

        } catch (SQLException e) {
            Ui.msgError(this, "Database error: " + e.getMessage());
        }
    }

    private boolean verifyOldPassword(String password) throws SQLException {
        String sql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        try (Connection c = AuthDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, session.userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    return PasswordHasher.verify(password, hash);
                }
                return false;
            }
        }
    }

    private void updatePasswordInDatabase(String newPassword) throws SQLException {
        String newHash = PasswordHasher.hash(newPassword);
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection c = AuthDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, session.userId);
            ps.executeUpdate();
        }
    }

    enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
