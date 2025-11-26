package edu.univ.erp.access;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.MaintenanceService;

public class AccessControl {
    public static void mustAllowWrite(String role) {
        if (MaintenanceService.isMaintenanceOn() && !role.equals("ADMIN")) {
            throw new RuntimeException("Maintenance is ON. Changes are temporarily disabled.");
        }
    }
    public static void mustBeInstructorOf(Session s, int instructorUserId) {
        if (!s.role.equals("ADMIN") && !(s.role.equals("INSTRUCTOR") && s.userId == instructorUserId)) {
            throw new RuntimeException("Not your section.");
        }
    }
    public static void mustBeStudent(Session s) {
        if (!"STUDENT".equals(s.role)) throw new RuntimeException("Not allowed.");
    }
    public static void mustBeAdmin(Session s) {
        if (!"ADMIN".equals(s.role)) throw new RuntimeException("Not allowed.");
    }
}
