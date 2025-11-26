package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.ErpDb;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class InstructorService {

    /**
     * Get all sections taught by the instructor.
     */
    public DefaultTableModel mySections(int instructorUserId) throws SQLException {
        String[] cols = { "Section ID", "Code", "Title", "Day/Time", "Room", "Capacity", "Enrolled", "Semester",
                "Year" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.capacity, " +
                "COALESCE(COUNT(e.enrollment_id), 0) AS enrolled, s.semester, s.year " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE s.instructor_user_id = ? " +
                "GROUP BY s.section_id, c.code, c.title, s.day_time, s.room, s.capacity, s.semester, s.year " +
                "ORDER BY c.code, s.day_time";

        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.addRow(new Object[] {
                            rs.getInt(1), // section_id
                            rs.getString(2), // code
                            rs.getString(3), // title
                            rs.getString(4), // day_time
                            rs.getString(5), // room
                            rs.getInt(6), // capacity
                            rs.getInt(7), // enrolled
                            rs.getString(8), // semester
                            rs.getInt(9) // year
                    });
                }
            }
        }
        return m;
    }

    /**
     * Get roster for a section with all grades.
     */
    public DefaultTableModel roster(int sectionId) throws SQLException {
        String[] cols = { "Enrollment ID", "Student ID", "Roll No", "Quiz", "Midterm", "EndSem", "Final" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT e.enrollment_id, e.student_user_id, s.roll_no, " +
                "MAX(CASE WHEN g.component='QUIZ' THEN g.score END) AS quiz, " +
                "MAX(CASE WHEN g.component='MIDTERM' THEN g.score END) AS midterm, " +
                "MAX(CASE WHEN g.component='ENDSEM' THEN g.score END) AS endsem, " +
                "MAX(CASE WHEN g.component='FINAL' THEN g.final_grade END) AS final_grade " +
                "FROM enrollments e " +
                "JOIN students s ON s.user_id = e.student_user_id " +
                "LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? " +
                "GROUP BY e.enrollment_id, e.student_user_id, s.roll_no " +
                "ORDER BY s.roll_no";

        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.addRow(new Object[] {
                            rs.getInt(1), // enrollment_id
                            rs.getInt(2), // student_user_id
                            rs.getString(3), // roll_no
                            rs.getObject(4), // quiz
                            rs.getObject(5), // midterm
                            rs.getObject(6), // endsem
                            rs.getObject(7) // final_grade
                    });
                }
            }
        }
        return m;
    }

    /**
     * Save or update scores for an enrollment.
     * Uses the weighting rule: Quiz 20%, Midterm 30%, EndSem 50%.
     */
    public void saveScores(Session s, int sectionId, int enrollmentId,
            Double quiz, Double midterm, Double endsem) throws SQLException {
        AccessControl.mustAllowWrite(s.role);
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

        try (Connection c = ErpDb.get()) {
            c.setAutoCommit(false);
            try {
                deleteComponentScores(c, enrollmentId);

                if (quiz != null && quiz >= 0 && quiz <= 100) {
                    insertScore(c, enrollmentId, "QUIZ", quiz);
                }
                if (midterm != null && midterm >= 0 && midterm <= 100) {
                    insertScore(c, enrollmentId, "MIDTERM", midterm);
                }
                if (endsem != null && endsem >= 0 && endsem <= 100) {
                    insertScore(c, enrollmentId, "ENDSEM", endsem);
                }

                // Calculate final grade using weights: 20% quiz, 30% midterm, 50% endsem
                double q = quiz != null ? quiz : 0;
                double m = midterm != null ? midterm : 0;
                double e = endsem != null ? endsem : 0;
                double finalGrade = Math.round((0.2 * q + 0.3 * m + 0.5 * e) * 100.0) / 100.0;

                deleteFinalGrade(c, enrollmentId);
                insertFinalGrade(c, enrollmentId, finalGrade);

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void insertScore(Connection c, int enrollmentId, String component, Double score) throws SQLException {
        String sql = "INSERT INTO grades(enrollment_id, component, score) VALUES(?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);
            ps.executeUpdate();
        }
    }

    private void insertFinalGrade(Connection c, int enrollmentId, double finalGrade) throws SQLException {
        String sql = "INSERT INTO grades(enrollment_id, component, final_grade) VALUES(?, 'FINAL', ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setDouble(2, finalGrade);
            ps.executeUpdate();
        }
    }

    private void deleteComponentScores(Connection c, int enrollmentId) throws SQLException {
        String sql = "DELETE FROM grades WHERE enrollment_id = ? AND component IN ('QUIZ', 'MIDTERM', 'ENDSEM')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    private void deleteFinalGrade(Connection c, int enrollmentId) throws SQLException {
        String sql = "DELETE FROM grades WHERE enrollment_id = ? AND component = 'FINAL'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    private int findInstructorOf(int sectionId) throws SQLException {
        String sql = "SELECT instructor_user_id FROM sections WHERE section_id = ?";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new RuntimeException("Section not found.");
                Object obj = rs.getObject(1);
                if (obj == null)
                    throw new RuntimeException("No instructor assigned to this section.");
                return rs.getInt(1);
            }
        }
    }

    /**
     * Get class statistics (average, min, max for final grades).
     */
    public DefaultTableModel classStats(int sectionId) throws SQLException {
        String[] cols = { "Statistic", "Value" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT COUNT(*) AS total, " +
                "ROUND(AVG(CASE WHEN g.component='FINAL' THEN g.final_grade END)::numeric, 2) AS avg_final, " +
                "ROUND(MIN(CASE WHEN g.component='FINAL' THEN g.final_grade END)::numeric, 2) AS min_final, " +
                "ROUND(MAX(CASE WHEN g.component='FINAL' THEN g.final_grade END)::numeric, 2) AS max_final " +
                "FROM enrollments e " +
                "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                "WHERE e.section_id = ?";

        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.addRow(new Object[] { "Total Enrolled", rs.getInt("total") });
                    Object avg = rs.getObject("avg_final");
                    m.addRow(new Object[] { "Average Final Grade", avg != null ? avg : "N/A" });
                    Object min = rs.getObject("min_final");
                    m.addRow(new Object[] { "Minimum Final Grade", min != null ? min : "N/A" });
                    Object max = rs.getObject("max_final");
                    m.addRow(new Object[] { "Maximum Final Grade", max != null ? max : "N/A" });
                }
            }
        }
        return m;
    }

    /**
     * Export grades as CSV file.
     */
    public void exportGradesCsv(int sectionId, String filePath) throws Exception {
        // Avoid joining users_auth from ERP DB. Fetch student IDs and roll numbers,
        // then resolve usernames from Auth DB before writing CSV.
        String sql = "SELECT s.user_id, s.roll_no, " +
                "MAX(CASE WHEN g.component='QUIZ' THEN g.score END) AS quiz, " +
                "MAX(CASE WHEN g.component='MIDTERM' THEN g.score END) AS midterm, " +
                "MAX(CASE WHEN g.component='ENDSEM' THEN g.score END) AS endsem, " +
                "MAX(CASE WHEN g.component='FINAL' THEN g.final_grade END) AS final_grade " +
                "FROM enrollments e " +
                "JOIN students s ON s.user_id = e.student_user_id " +
                "LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? " +
                "GROUP BY s.user_id, s.roll_no " +
                "ORDER BY s.roll_no";

        // Collect rows and student IDs
        List<Object[]> rows = new ArrayList<>();
        Set<Integer> studentIds = new HashSet<>();
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt(1);
                    studentIds.add(uid);
                    rows.add(new Object[] { uid, rs.getString(2), rs.getObject(3), rs.getObject(4), rs.getObject(5),
                            rs.getObject(6) });
                }
            }
        }

        Map<Integer, String> names = new HashMap<>();
        try {
            names = edu.univ.erp.data.AuthLookup.usernamesForIds(studentIds);
        } catch (SQLException e) {
            System.err.println("Auth lookup failed: " + e.getMessage());
        }

        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("Roll No,Username,Quiz,Midterm,EndSem,Final Grade\n");
            for (Object[] r : rows) {
                int uid = (Integer) r[0];
                String roll = (String) r[1];
                String uname = names.getOrDefault(uid, "");
                fw.write(roll + "," + uname + "," +
                        (r[2] != null ? r[2].toString() : "") + "," +
                        (r[3] != null ? r[3].toString() : "") + "," +
                        (r[4] != null ? r[4].toString() : "") + "," +
                        (r[5] != null ? r[5].toString() : "") + "\n");
            }
        }
    }
}
