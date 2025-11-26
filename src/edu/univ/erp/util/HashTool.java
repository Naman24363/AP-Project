package edu.univ.erp.util;

import edu.univ.erp.auth.PasswordHasher;

public class HashTool {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java edu.univ.erp.util.HashTool <password>");
            return;
        }
        String pass = args[0];
        String hash = PasswordHasher.hash(pass);
        System.out.println(hash);
    }
}
