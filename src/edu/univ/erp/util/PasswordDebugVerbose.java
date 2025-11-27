package edu.univ.erp.util;

import edu.univ.erp.data.AuthDb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class PasswordDebugVerbose {
    public static void main(String[] args) {
        try (Connection c = AuthDb.get();
                PreparedStatement ps = c
                        .prepareStatement("SELECT username, password_hash FROM users_auth ORDER BY user_id")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    String stored = rs.getString("password_hash");
                    System.out.println("User: " + user);
                    System.out.println("Stored: " + stored);
                    String[] p = stored.split("\\$");
                    if (p.length != 4) {
                        System.out.println("  Invalid stored format (parts=" + p.length + ")");
                        continue;
                    }
                    String alg = p[0];
                    String iters = p[1];
                    String saltB64 = p[2];
                    String dkB64 = p[3];
                    System.out.println("  alg=" + alg + " iters=" + iters);
                    byte[] salt = Base64.getDecoder().decode(saltB64);
                    byte[] expected = Base64.getDecoder().decode(dkB64);
                    System.out.println("  salt.len=" + salt.length + " expected.len=" + expected.length);

                    String testPw = switch (user) {
                        case "admin1" -> "admin123";
                        case "inst1" -> "inst123";
                        case "stu1" -> "student1";
                        case "stu2" -> "student2";
                        default -> "";
                    };
                    try {
                        byte[] got = edu.univ.erp.auth.PasswordHasher.pbkdf2ForDebug(stored, testPw);
                        String gotB64 = Base64.getEncoder().encodeToString(got);
                        System.out.println("  computed.b64=" + gotB64);
                        System.out.println("  expected.b64=" + dkB64);
                        boolean equal = java.util.Arrays.equals(got, expected);
                        System.out.println("  equal(bytes)=" + equal);
                    } catch (Exception e) {
                        System.out.println("  compute failed: " + e.getMessage());
                        e.printStackTrace(System.out);
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.err.println("PasswordDebugVerbose failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
