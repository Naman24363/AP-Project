package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.ErpDb;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;

public class StudentService {
    public DefaultTableModel myRegistrations(int studentUserId) throws SQLException {
        // We'll include the internal enrollment_id in the model (hidden in the UI)
        // and display the instructor name as the visible first column.
        String[] cols = { "Enrollment ID", "Instructor", "Section ID", "Code", "Title", "Day/Time", "Room", "Status" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT e.enrollment_id, s.section_id, c.code, c.title, s.day_time, s.room, e.status, s.instructor_user_id "
                +
                "FROM enrollments e JOIN sections s ON e.section_id=s.section_id " +
                "JOIN courses c ON s.course_id=c.course_id WHERE e.student_user_id=? ORDER BY c.code";

        // Collect rows and instructor ids to resolve usernames from Auth DB
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        java.util.Set<Integer> instrIds = new java.util.HashSet<>();

        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer instr = (Integer) rs.getObject(8);
                    if (instr != null)
                        instrIds.add(instr);
                    rows.add(new Object[] {
                            rs.getInt(1), // enrollment_id
                            instr, // instructor_user_id placeholder
                            rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                            rs.getString(7)
                    });
                }
            }
        }

        // Resolve instructor usernames from Auth DB
        java.util.Map<Integer, String> names = new java.util.HashMap<>();
        try {
            names = edu.univ.erp.data.AuthLookup.usernamesForIds(instrIds);
        } catch (SQLException e) {
            System.err.println("Auth lookup failed: " + e.getMessage());
        }

        for (Object[] r : rows) {
            Integer instr = (Integer) r[1];
            String uname = instr != null ? names.getOrDefault(instr, "Unassigned") : "Unassigned";
            m.addRow(new Object[] { r[0], uname, r[2], r[3], r[4], r[5], r[6], r[7] });
        }

        return m;
    }

    public void register(Session s, int sectionId) throws SQLException {
        AccessControl.mustBeStudent(s);
        AccessControl.mustAllowWrite(s.role);
        String checkFull = "SELECT capacity - COALESCE((SELECT COUNT(*) FROM enrollments e WHERE e.section_id=s.section_id),0) AS seats_left "
                +
                "FROM sections s WHERE s.section_id=?";
        String dupCheck = "SELECT 1 FROM enrollments WHERE student_user_id=? AND section_id=?";
        try (Connection c = ErpDb.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement psFull = c.prepareStatement(checkFull);
                    PreparedStatement psDup = c.prepareStatement(dupCheck)) {
                psFull.setInt(1, sectionId);
                try (ResultSet rs = psFull.executeQuery()) {
                    if (!rs.next())
                        throw new RuntimeException("Section not found.");
                    if (rs.getInt(1) <= 0)
                        throw new RuntimeException("Section full.");
                }
                psDup.setInt(1, s.userId);
                psDup.setInt(2, sectionId);
                try (ResultSet rs2 = psDup.executeQuery()) {
                    if (rs2.next())
                        throw new RuntimeException("Already registered for this section.");
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO enrollments(student_user_id, section_id, status) VALUES(?,?, 'ENROLLED')")) {
                    ins.setInt(1, s.userId);
                    ins.setInt(2, sectionId);
                    ins.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void drop(Session s, int enrollmentId) throws SQLException {
        AccessControl.mustBeStudent(s);
        AccessControl.mustAllowWrite(s.role);
        LocalDate ddl = MaintenanceService.dropDeadline();
        if (LocalDate.now().isAfter(ddl))
            throw new RuntimeException("Drop deadline passed.");
        String sql = "DELETE FROM enrollments WHERE enrollment_id=? AND student_user_id=?";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, s.userId);
            int n = ps.executeUpdate();
            if (n == 0)
                throw new RuntimeException("Not found or not your enrollment.");
        }
    }

    public DefaultTableModel grades(int studentUserId) throws SQLException {
        String[] cols = { "Course", "Section", "Component", "Score", "Final" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        String sql = "SELECT c.code||' '||c.title AS course, e.section_id, g.component, g.score, g.final_grade " +
                "FROM grades g JOIN enrollments e ON g.enrollment_id=e.enrollment_id " +
                "JOIN sections s ON e.section_id=s.section_id JOIN courses c ON s.course_id=c.course_id " +
                "WHERE e.student_user_id=? ORDER BY course, component";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.addRow(new Object[] { rs.getString(1), rs.getInt(2), rs.getString(3), rs.getBigDecimal(4),
                            rs.getBigDecimal(5) });
                }
            }
        }
        return m;
    }

    public void exportTranscriptCsv(int studentUserId, String filePath) throws Exception {
        String sql = "SELECT c.code, c.title, c.credits, MAX(g.final_grade) AS final " +
                "FROM enrollments e JOIN sections s ON e.section_id=s.section_id " +
                "JOIN courses c ON s.course_id=c.course_id " +
                "LEFT JOIN grades g ON g.enrollment_id=e.enrollment_id " +
                "WHERE e.student_user_id=? GROUP BY c.code,c.title,c.credits ORDER BY c.code";
        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement(sql);
                FileWriter fw = new FileWriter(filePath)) {
            ps.setInt(1, studentUserId);
            fw.write("Code,Title,Credits,Final\n");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fw.write(rs.getString(1) + "," + rs.getString(2) + "," + rs.getInt(3) + "," +
                            (rs.getObject(4) == null ? "" : rs.getBigDecimal(4)) + "\n");
                }
            }
        }
    }
}
