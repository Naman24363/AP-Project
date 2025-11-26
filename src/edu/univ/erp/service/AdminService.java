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
            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, role);
            ps.setString(4, hash);
            ps.executeUpdate();
            return userId;
        }
    }

    public void createStudentProfile(Session s, int userId, String rollNo, String program, int year)
            throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c
                        .prepareStatement("INSERT INTO students(user_id, roll_no, program, year) VALUES(?,?,?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            ps.setInt(4, year);
            ps.executeUpdate();
        }
    }

    public void createInstructorProfile(Session s, int userId, String department) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("INSERT INTO instructors(user_id, department) VALUES(?,?)")) {
            ps.setInt(1, userId);
            ps.setString(2, department);
            ps.executeUpdate();
        }
    }

    public int createCourse(Session s, String code, String title, int credits) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("INSERT INTO courses(code,title,credits) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int createSection(Session s, int courseId, Integer instructorUserId, String dayTime, String room,
            int capacity, String semester, int year) throws SQLException {
        AccessControl.mustBeAdmin(s);
        if (capacity < 0)
            throw new RuntimeException("Capacity cannot be negative.");
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO sections(course_id, instructor_user_id, day_time, room, capacity, semester, year) VALUES(?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            if (instructorUserId == null)
                ps.setNull(2, java.sql.Types.INTEGER);
            else
                ps.setInt(2, instructorUserId);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void assignInstructor(Session s, int sectionId, int instructorUserId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c
                        .prepareStatement("UPDATE sections SET instructor_user_id=? WHERE section_id=?")) {
            ps.setInt(1, instructorUserId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        }
    }

    /**
     * Return next available user_id for Auth DB (max(user_id)+1).
     */
    public int nextUserId() throws SQLException {
        try (Connection c = AuthDb.get();
                PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(user_id),0)+1 FROM users_auth")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        throw new SQLException("Could not determine next user id");
    }

    /**
     * Return map of instructor user_id -> username for all instructors.
     */
    public java.util.Map<Integer, String> getAllInstructors() throws SQLException {
        java.util.Map<Integer, String> map = new java.util.HashMap<>();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        String sql = "SELECT user_id FROM instructors ORDER BY user_id";
        try (Connection c = ErpDb.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                ids.add(rs.getInt(1));
        }
        if (ids.isEmpty())
            return map;
        // resolve usernames from Auth DB
        java.util.Set<Integer> idset = new java.util.HashSet<>(ids);
        try {
            map.putAll(edu.univ.erp.data.AuthLookup.usernamesForIds(idset));
        } catch (SQLException e) {
            // ignore lookup failure; return empty map
            System.err.println("Auth lookup failed: " + e.getMessage());
        }
        return map;
    }

    public void deleteCourse(Session s, int courseId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        // Prevent deleting courses that have sections
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM sections WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("Cannot delete course with existing sections. Remove sections first.");
                }
            }
        }
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            int affected = ps.executeUpdate();
            if (affected == 0)
                throw new RuntimeException("Course not found.");
        }
    }

    public void deleteInstructor(Session s, int instructorUserId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        // Prevent deleting instructor if assigned to sections
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c
                        .prepareStatement("SELECT COUNT(*) FROM sections WHERE instructor_user_id = ?")) {
            ps.setInt(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException(
                            "Cannot delete instructor assigned to sections. Unassign or remove sections first.");
                }
            }
        }
        // delete instructor profile
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM instructors WHERE user_id = ?")) {
            ps.setInt(1, instructorUserId);
            ps.executeUpdate();
        }
        // delete auth user
        try (Connection c = AuthDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM users_auth WHERE user_id = ?")) {
            ps.setInt(1, instructorUserId);
            ps.executeUpdate();
        }
    }

    /**
     * Return a list of section ids assigned to the given instructor.
     */
    public java.util.List<Integer> getSectionsForInstructor(int instructorUserId) throws SQLException {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        String sql = "SELECT section_id FROM sections WHERE instructor_user_id = ? ORDER BY section_id";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(rs.getInt(1));
            }
        }
        return list;
    }

    /**
     * Return a list of section ids for the given course.
     */
    public java.util.List<Integer> getSectionsForCourse(int courseId) throws SQLException {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        String sql = "SELECT section_id FROM sections WHERE course_id = ? ORDER BY section_id";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(rs.getInt(1));
            }
        }
        return list;
    }

    /**
     * Unassign the instructor from all their sections (set instructor_user_id =
     * NULL).
     * Returns the number of sections updated.
     */
    public int unassignInstructorFromSections(Session s, int instructorUserId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c
                        .prepareStatement(
                                "UPDATE sections SET instructor_user_id = NULL WHERE instructor_user_id = ?")) {
            ps.setInt(1, instructorUserId);
            return ps.executeUpdate();
        }
    }

    /**
     * Delete a section by id. Prevent deletion if there are enrollments.
     */
    public void deleteSection(Session s, int sectionId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        // prevent deleting sections with enrollments
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM enrollments WHERE section_id = ?")) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("Cannot delete section with enrollments. Remove enrollments first.");
                }
            }
        }
        // delete any per-section settings (weights)
        String key = "weights_section_" + sectionId;
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            ps.executeUpdate();
        }
        // delete the section row
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
            ps.setInt(1, sectionId);
            int affected = ps.executeUpdate();
            if (affected == 0)
                throw new RuntimeException("Section not found.");
        }
    }

    /**
     * Delete all sections for a given course. Prevents deletion if any section has
     * enrollments.
     * Returns the number of sections deleted.
     */
    public int deleteSectionsForCourse(Session s, int courseId) throws SQLException {
        AccessControl.mustBeAdmin(s);
        // prevent deleting sections with enrollments
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM enrollments WHERE section_id IN (SELECT section_id FROM sections WHERE course_id = ?)")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("Cannot delete sections with enrollments. Remove enrollments first.");
                }
            }
        }

        // get all section ids for this course
        java.util.List<Integer> sectionIds = getSectionsForCourse(courseId);
        if (sectionIds.isEmpty()) {
            return 0;
        }

        // delete per-section settings for all sections
        try (Connection c = ErpDb.get()) {
            for (Integer sectionId : sectionIds) {
                String key = "weights_section_" + sectionId;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM settings WHERE key = ?")) {
                    ps.setString(1, key);
                    ps.executeUpdate();
                }
            }
        }

        // delete all sections for this course
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement("DELETE FROM sections WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            return ps.executeUpdate();
        }
    }

    /**
     * Purge all sections and courses (and related per-section settings,
     * enrollments, grades).
     * This is destructive and runs in a single transaction.
     */
    public void purgeAllCoursesAndSections(Session s) throws SQLException {
        AccessControl.mustBeAdmin(s);
        try (Connection c = ErpDb.get()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // remove per-section settings
                try (PreparedStatement ps = c
                        .prepareStatement("DELETE FROM settings WHERE key LIKE 'weights_section_%'")) {
                    ps.executeUpdate();
                }
                // delete grades tied to enrollments in any section
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM grades WHERE enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE section_id IN (SELECT section_id FROM sections))")) {
                    ps.executeUpdate();
                }
                // delete enrollments for sections
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM enrollments WHERE section_id IN (SELECT section_id FROM sections)")) {
                    ps.executeUpdate();
                }
                // delete sections
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM sections")) {
                    ps.executeUpdate();
                }
                // delete courses
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM courses")) {
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(oldAuto);
            }
        }
    }
}
