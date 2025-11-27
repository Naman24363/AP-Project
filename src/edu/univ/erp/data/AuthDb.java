package edu.univ.erp.data;

import edu.univ.erp.util.Config;
import java.sql.*;

public class AuthDb {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }
    }

    public static Connection get() throws SQLException {
        String url = Config.get("auth.url");
        String user = Config.get("auth.user");
        String pass = Config.get("auth.password");

        if (url == null || url.isEmpty() || user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            throw new SQLException("Database configuration missing or incomplete (check config/app.properties)");
        }

        try {
            DriverManager.setLoginTimeout(5);
        } catch (Exception ignored) {
        }

        return DriverManager.getConnection(
                Config.get("auth.url"), Config.get("auth.user"), Config.get("auth.password"));
    }
}
