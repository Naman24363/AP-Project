import java.security.MessageDigest;

public class HashGen {
    public static void main(String[] args) {
        String password = "Shaina@601";
        String hash = PasswordHasher.hash(password);
        System.out.println("Password hash: " + hash);
    }

    // Local PasswordHasher to avoid relying on external classpath
    static class PasswordHasher {
        public static String hash(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] bytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                throw new RuntimeException("Hashing failed", e);
            }
        }
    }
}
