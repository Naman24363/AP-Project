package edu.univ.erp.auth;

import edu.univ.erp.data.AuthDb;
import java.sql.*;
import java.time.Instant;

public class AuthService {
    public Session login(String username, String password) throws SQLException {
        String sql = "SELECT user_id, role, password_hash, status FROM users_auth WHERE username = ?";
        try (Connection c = AuthDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Incorrect username or password.");
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String hash = rs.getString("password_hash");
                String status = rs.getString("status");
                if (!"ACTIVE".equalsIgnoreCase(status)) throw new RuntimeException("Account is not active.");
                if (!PasswordHasher.verify(password, hash)) throw new RuntimeException("Incorrect username or password.");
                try (PreparedStatement upd = c.prepareStatement("UPDATE users_auth SET last_login=? WHERE user_id=?")) {
                    upd.setTimestamp(1, Timestamp.from(Instant.now()));
                    upd.setInt(2, userId);
                    upd.executeUpdate();
                }
                return new Session(userId, username, role);
            }
        }
    }
}
