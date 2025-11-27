package edu.univ.erp.util;

import edu.univ.erp.auth.PasswordHasher;

public class GenerateAuthHash {
    public static void main(String[] args) {
        String[] users = { "admin1", "inst1", "stu1", "stu2" };
        String[] pw = { "admin123", "inst123", "student1", "student2" };

        for (int i = 0; i < users.length; i++) {
            String h = PasswordHasher.hash(pw[i]);
            System.out.println("-- For user: " + users[i]);
            System.out.println("UPDATE users_auth SET password_hash='" + h + "' WHERE username='" + users[i] + "';");
            System.out.println();
        }
    }
}
