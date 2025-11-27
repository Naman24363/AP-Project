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

        contentPanel.add(createFormGroup(
                "Current Password",
                pfOldPassword = Ui.createPasswordField(25)));

        contentPanel.add(Box.createVerticalStrut(12));

        contentPanel.add(createFormGroup(
                "New Password",
                pfNewPassword = Ui.createPasswordField(25)));

        contentPanel.add(Box.createVerticalStrut(8));

        contentPanel.add(createFormGroup(
                "Confirm New Password",
                pfConfirmPassword = Ui.createPasswordField(25)));
        contentPanel.add(Box.createVerticalStrut(20));

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

    private void changePassword() {
        String oldPassword = new String(pfOldPassword.getPassword());
        String newPassword = new String(pfNewPassword.getPassword());
        String confirmPassword = new String(pfConfirmPassword.getPassword());

        if (oldPassword.isEmpty()) {
            Ui.msgError(this, "Please enter your current password.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Ui.msgError(this, "New passwords do not match.");
            return;
        }

        try {
            if (!verifyOldPassword(oldPassword)) {
                Ui.msgError(this, "Current password is incorrect.");
                return;
            }

            updatePasswordInDatabase(newPassword);
            Ui.msgSuccess(this, "Password changed successfully!");

            pfOldPassword.setText("");
            pfNewPassword.setText("");
            pfConfirmPassword.setText("");

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
}
