package edu.univ.erp.util;

import edu.univ.erp.data.AuthDb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordDebugAlgo {
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
                        System.out.println("  Invalid format");
                        continue;
                    }
                    int it = Integer.parseInt(p[1]);
                    byte[] salt = Base64.getDecoder().decode(p[2]);
                    byte[] expected = Base64.getDecoder().decode(p[3]);
                    String testPw = switch (user) {
                        case "admin1" -> "admin123";
                        case "inst1" -> "inst123";
                        case "stu1" -> "student1";
                        case "stu2" -> "student2";
                        default -> "";
                    };
                    tryAlgorithms(testPw.toCharArray(), salt, it, expected.length * 8, expected);
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.err.println("PasswordDebugAlgo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void tryAlgorithms(char[] password, byte[] salt, int iterations, int keyLenBits, byte[] expected) {
        String[] algs = { "PBKDF2WithHmacSHA1", "PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA512" };
        for (String alg : algs) {
            try {
                PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
                SecretKeyFactory skf = SecretKeyFactory.getInstance(alg);
                byte[] dk = skf.generateSecret(spec).getEncoded();
                String b64 = Base64.getEncoder().encodeToString(dk);
                boolean eq = java.util.Arrays.equals(dk, expected);
                System.out.println("  alg=" + alg + " -> " + b64 + " equal=" + eq);
            } catch (Exception e) {
                System.out.println("  alg=" + alg + " -> error: " + e.getMessage());
            }
        }
    }
}
