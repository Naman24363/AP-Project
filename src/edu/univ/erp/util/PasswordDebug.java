package edu.univ.erp.util;

import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.AuthDb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Simple debug helper to verify seeded password hashes against expected test
 * passwords.
 * Run after compiling the project to check whether PasswordHasher.verify(...)
 * works
 * with the seeded values in the `users_auth` table.
 */
public class PasswordDebug {
    public static void main(String[] args) {
        try (Connection c = AuthDb.get();
                PreparedStatement ps = c
                        .prepareStatement("SELECT username, password_hash FROM users_auth ORDER BY user_id")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    String testPw = switch (user) {
                        case "admin1" -> "admin123";
                        case "inst1" -> "inst123";
                        case "stu1" -> "student1";
                        case "stu2" -> "student2";
                        default -> "";
                    };
                    boolean ok = PasswordHasher.verify(testPw, hash);
                    System.out.println(user + " -> verify(" + testPw + ") = " + ok);
                }
            }
        } catch (Exception e) {
            System.err.println("PasswordDebug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
