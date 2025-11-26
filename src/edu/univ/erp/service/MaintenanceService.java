package edu.univ.erp.service;

import edu.univ.erp.data.ErpDb;
import java.sql.*;

public class MaintenanceService {
    public static boolean isMaintenanceOn() {
        String sql = "SELECT value FROM settings WHERE key='maintenance_on'";
        try (Connection c = ErpDb.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return "true".equalsIgnoreCase(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("Error checking maintenance status: " + e.getMessage());
        }
        return false;
    }
    public void setMaintenance(boolean on) throws SQLException {
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO settings(key,value) VALUES('maintenance_on',?) ON CONFLICT (key) DO UPDATE SET value=EXCLUDED.value")) {
            ps.setString(1, on ? "true" : "false");
            ps.executeUpdate();
        }
    }
    public static java.time.LocalDate dropDeadline() {
        String sql = "SELECT value FROM settings WHERE key='drop_deadline'";
        try (Connection c = ErpDb.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return java.time.LocalDate.parse(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("Error retrieving drop deadline: " + e.getMessage());
        }
        return java.time.LocalDate.now().minusDays(1);
    }
}
