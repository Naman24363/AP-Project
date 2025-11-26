package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.AuthDb;
import edu.univ.erp.data.ErpDb;

import java.sql.*;

public class AdminService {
    public void toggleMaintenance(Session s, boolean on) throws SQLException {
        AccessControl.mustBeAdmin(s);
        new MaintenanceService().setMaintenance(on);
    }

    public int createUser(Session s, int userId, String username, String role, String rawPassword) throws SQLException {
        AccessControl.mustBeAdmin(s);
        String hash = PasswordHasher.hash(rawPassword);
        try (Connection c = AuthDb.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users_auth(user_id, username, role, password_hash, status) VALUES(?,?,?,?, 'ACTIVE')")) {
            ps.setInt(1, userId); ps.setString(2, username); ps.setString(3, role); ps.setString(4, hash);
            ps.executeUpdate();
            return userId;
        }
    }

    public void createStudentProfile(Session s, int userId, String rollNo, String program, int year) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO students(user_id, roll_no, program, year) VALUES(?,?,?,?)")) {
            ps.setInt(1, userId); ps.setString(2, rollNo); ps.setString(3, program); ps.setInt(4, year);
            ps.executeUpdate();
        }
    }

    public void createInstructorProfile(Session s, int userId, String department) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO instructors(user_id, department) VALUES(?,?)")) {
            ps.setInt(1, userId); ps.setString(2, department); ps.executeUpdate();
        }
    }

    public int createCourse(Session s, String code, String title, int credits) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO courses(code,title,credits) VALUES(?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code); ps.setString(2, title); ps.setInt(3, credits); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }

    public int createSection(Session s, int courseId, Integer instructorUserId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        AccessControl.mustBeAdmin(s);
        if (capacity < 0) throw new RuntimeException("Capacity cannot be negative.");
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO sections(course_id, instructor_user_id, day_time, room, capacity, semester, year) VALUES(?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            if (instructorUserId==null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, instructorUserId);
            ps.setString(3, dayTime); ps.setString(4, room); ps.setInt(5, capacity); ps.setString(6, semester); ps.setInt(7, year);
            ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }

    public void assignInstructor(Session s, int sectionId, int instructorUserId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
             PreparedStatement ps = c.prepareStatement("UPDATE sections SET instructor_user_id=? WHERE section_id=?")) {
            ps.setInt(1, instructorUserId); ps.setInt(2, sectionId); ps.executeUpdate();
        }
    }
}
