package edu.univ.erp.auth;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    private static final int ITERATIONS = 120000;
    private static final int KEY_LEN = 256;
    private static final SecureRandom RNG = new SecureRandom();

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        byte[] dk = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LEN);
        return "PBKDF2$" + ITERATIONS + "$" + b64(salt) + "$" + b64(dk);
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] p = stored.split("\\$");
            if (p.length != 4 || !p[0].equals("PBKDF2"))
                return false;
            int it = Integer.parseInt(p[1]);
            byte[] salt = Base64.getDecoder().decode(p[2]);
            byte[] expected = Base64.getDecoder().decode(p[3]);
            byte[] got = pbkdf2(password.toCharArray(), salt, it, expected.length * 8);
            if (got.length != expected.length)
                return false;
            int diff = 0;
            for (int i = 0; i < got.length; i++)
                diff |= got[i] ^ expected[i];
            return diff == 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 failure", e);
        }
    }

    private static String b64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }

    // Debug helper: compute the derived key bytes for a stored hash string
    public static byte[] pbkdf2ForDebug(String stored, String password) {
        String[] p = stored.split("\\$");
        if (p.length != 4 || !p[0].equals("PBKDF2"))
            throw new IllegalArgumentException("Invalid stored hash");
        int it = Integer.parseInt(p[1]);
        byte[] salt = Base64.getDecoder().decode(p[2]);
        byte[] expected = Base64.getDecoder().decode(p[3]);
        return pbkdf2(password.toCharArray(), salt, it, expected.length * 8);
    }
}
