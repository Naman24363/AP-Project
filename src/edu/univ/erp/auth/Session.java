package edu.univ.erp.auth;

public class Session {
    private static volatile Session current;
    public final int userId;
    public final String username;
    public final String role;

    Session(int userId, String username, String role) {
        this.userId = userId; this.username = username; this.role = role;
    }
    public static void set(Session s) { current = s; }
    public static Session get() { return current; }
}
