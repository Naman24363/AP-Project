package edu.univ.erp.data;

import edu.univ.erp.util.Config;
import java.sql.*;

public class ErpDb {
    static {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("PostgreSQL driver not found", e); }
    }
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
            Config.get("erp.url"), Config.get("erp.user"), Config.get("erp.password"));
    }
}
