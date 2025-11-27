package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.AuthDb;
import edu.univ.erp.data.ErpDb;
import java.io.BufferedReader;
import java.io.FileReader;
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
    public DefaultTableModel roster(Session s, int sectionId) throws SQLException {
        String[] cols = { "Enrollment ID", "Student ID", "Roll No", "Quiz", "Midterm", "EndSem", "Final" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // enforce access: only instructor of this section or admin
        int instrId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instrId);

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

                // Calculate final grade using stored weights (percentages)
                double[] weights = getWeights(sectionId); // returns fractions summing to 1.0
                double q = quiz != null ? quiz : 0;
                double m = midterm != null ? midterm : 0;
                double e = endsem != null ? endsem : 0;
                double finalGrade = Math.round((weights[0] * q + weights[1] * m + weights[2] * e) * 100.0) / 100.0;

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

    /**
     * Retrieve weight fractions for a section. Returns {quiz, midterm, endsem} as
     * fractions (sum to 1.0).
     * If no custom weights found, returns default 0.2,0.3,0.5
     */
    public double[] getWeights(int sectionId) {
        String key = "weights_section_" + sectionId;
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String v = rs.getString(1);
                    // expected format: "q,m,e" as integers or decimals representing percentages
                    String[] parts = v.split(",");
                    if (parts.length == 3) {
                        double q = Double.parseDouble(parts[0]) / 100.0;
                        double m = Double.parseDouble(parts[1]) / 100.0;
                        double e = Double.parseDouble(parts[2]) / 100.0;
                        double sum = q + m + e;
                        if (sum > 0) {
                            return new double[] { q / sum, m / sum, e / sum };
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Could not read weights for section " + sectionId + ": " + ex.getMessage());
        }
        return new double[] { 0.2, 0.3, 0.5 };
    }

    /**
     * Persist weights (percent integers) for a section. Must be called by an
     * instructor or admin.
     */
    public void setWeights(Session s, int sectionId, int qPercent, int mPercent, int ePercent) throws SQLException {
        AccessControl.mustAllowWrite(s.role);
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

        String key = "weights_section_" + sectionId;
        String sql = "INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT (key) DO UPDATE SET value=EXCLUDED.value";
        try (Connection c = ErpDb.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, qPercent + "," + mPercent + "," + ePercent);
            ps.executeUpdate();
        }
        // After updating weights, recompute finals for enrolled students
        recomputeFinals(s, sectionId);
    }

    /**
     * Recompute FINAL component for all enrollments in a section using stored
     * weights.
     */
    public void recomputeFinals(Session s, int sectionId) throws SQLException {
        AccessControl.mustAllowWrite(s.role);
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

        String sql = "SELECT e.enrollment_id, " +
                "MAX(CASE WHEN g.component='QUIZ' THEN g.score END) AS quiz, " +
                "MAX(CASE WHEN g.component='MIDTERM' THEN g.score END) AS midterm, " +
                "MAX(CASE WHEN g.component='ENDSEM' THEN g.score END) AS endsem " +
                "FROM enrollments e " +
                "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                "WHERE e.section_id = ? GROUP BY e.enrollment_id";

        double[] weights = getWeights(sectionId);
        try (Connection c = ErpDb.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int enrollmentId = rs.getInt(1);
                        double q = rs.getObject(2) != null ? rs.getDouble(2) : 0.0;
                        double m = rs.getObject(3) != null ? rs.getDouble(3) : 0.0;
                        double e = rs.getObject(4) != null ? rs.getDouble(4) : 0.0;
                        double finalGrade = Math.round((weights[0] * q + weights[1] * m + weights[2] * e) * 100.0)
                                / 100.0;
                        deleteFinalGrade(c, enrollmentId);
                        insertFinalGrade(c, enrollmentId, finalGrade);
                    }
                }
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
    public DefaultTableModel classStats(Session s, int sectionId) throws SQLException {
        // enforce access
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

        String[] cols = { "Statistic", "Value" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT COUNT(DISTINCT e.enrollment_id) AS total, " +
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
    public void exportGradesCsv(Session s, int sectionId, String filePath) throws Exception {
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

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

    /**
     * Import grades from a CSV file. Expected header containing either 'Roll No' or
     * 'Username',
     * and columns 'Quiz','Midterm','EndSem' (case-insensitive). Returns list of
     * status messages
     * (one per input row) describing success or error for that row.
     */
    public java.util.List<String> importGradesCsv(Session s, int sectionId, String filePath) throws Exception {
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);
        AccessControl.mustAllowWrite(s.role);

        java.util.List<String> results = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null)
                throw new RuntimeException("Empty CSV file");

            java.util.List<String> headers = parseCsvLine(header);
            int idxRoll = -1, idxUser = -1, idxQuiz = -1, idxMid = -1, idxEnd = -1;
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).trim().toLowerCase();
                if (h.equals("roll no") || h.equals("roll") || h.equals("roll_no"))
                    idxRoll = i;
                if (h.equals("username"))
                    idxUser = i;
                if (h.equals("quiz"))
                    idxQuiz = i;
                if (h.equals("midterm") || h.equals("mid"))
                    idxMid = i;
                if (h.equals("endsem") || h.equals("end_sem") || h.equals("end sem") || h.equals("end"))
                    idxEnd = i;
            }

            if (idxQuiz == -1 && idxMid == -1 && idxEnd == -1)
                throw new RuntimeException("CSV must contain at least one of Quiz/Midterm/EndSem columns");

            String line;
            int lineno = 1;
            while ((line = br.readLine()) != null) {
                lineno++;
                java.util.List<String> parts = parseCsvLine(line);
                try {
                    String roll = idxRoll != -1 && idxRoll < parts.size() ? parts.get(idxRoll).trim() : "";
                    String uname = idxUser != -1 && idxUser < parts.size() ? parts.get(idxUser).trim() : "";

                    Integer studentUserId = null;
                    if (!roll.isEmpty()) {
                        try (Connection c = ErpDb.get();
                                PreparedStatement ps = c
                                        .prepareStatement("SELECT user_id FROM students WHERE roll_no = ?")) {
                            ps.setString(1, roll);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                    studentUserId = rs.getInt(1);
                            }
                        }
                    }
                    if (studentUserId == null && !uname.isEmpty()) {
                        try (Connection c = AuthDb.get();
                                PreparedStatement ps = c
                                        .prepareStatement("SELECT user_id FROM users_auth WHERE username = ?")) {
                            ps.setString(1, uname);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next())
                                    studentUserId = rs.getInt(1);
                            }
                        }
                    }

                    if (studentUserId == null) {
                        results.add("Line " + lineno + ": student not found (roll='" + roll + "' username='" + uname
                                + "')");
                        continue;
                    }

                    // find enrollment
                    Integer enrollmentId = null;
                    try (Connection c = ErpDb.get();
                            PreparedStatement ps = c.prepareStatement(
                                    "SELECT enrollment_id FROM enrollments WHERE student_user_id = ? AND section_id = ?")) {
                        ps.setInt(1, studentUserId);
                        ps.setInt(2, sectionId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                enrollmentId = rs.getInt(1);
                        }
                    }

                    if (enrollmentId == null) {
                        results.add("Line " + lineno + ": student not enrolled in section");
                        continue;
                    }

                    Double quiz = idxQuiz != -1 && idxQuiz < parts.size() && !parts.get(idxQuiz).trim().isEmpty()
                            ? Double.valueOf(parts.get(idxQuiz).trim())
                            : null;
                    Double mid = idxMid != -1 && idxMid < parts.size() && !parts.get(idxMid).trim().isEmpty()
                            ? Double.valueOf(parts.get(idxMid).trim())
                            : null;
                    Double end = idxEnd != -1 && idxEnd < parts.size() && !parts.get(idxEnd).trim().isEmpty()
                            ? Double.valueOf(parts.get(idxEnd).trim())
                            : null;

                    // validate ranges if provided
                    if ((quiz != null && (quiz < 0 || quiz > 100)) || (mid != null && (mid < 0 || mid > 100))
                            || (end != null && (end < 0 || end > 100))) {
                        results.add("Line " + lineno + ": invalid score (must be 0-100)");
                        continue;
                    }

                    // call saveScores to persist and compute final
                    saveScores(s, sectionId, enrollmentId, quiz, mid, end);
                    results.add("Line " + lineno + ": OK");
                } catch (Exception ex) {
                    results.add("Line " + lineno + ": error: " + ex.getMessage());
                }
            }
        }

        return results;
    }

    private java.util.List<String> parseCsvLine(String line) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (line == null)
            return out;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }

    /**
     * Preview CSV import: parse rows and resolve student/enrollment info but do not
     * persist.
     * Returns rows as String[]: {Roll, Username, StudentId, Enrolled(YES/NO), Quiz,
     * Midterm, EndSem, Error}
     */
    public java.util.List<String[]> previewGradesCsv(Session s, int sectionId, String filePath) throws Exception {
        int instructorUserId = findInstructorOf(sectionId);
        AccessControl.mustBeInstructorOf(s, instructorUserId);

        java.util.List<String[]> out = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header == null)
                throw new RuntimeException("Empty CSV file");

            java.util.List<String> headers = parseCsvLine(header);
            int idxRoll = -1, idxUser = -1, idxQuiz = -1, idxMid = -1, idxEnd = -1;
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).trim().toLowerCase();
                if (h.equals("roll no") || h.equals("roll") || h.equals("roll_no"))
                    idxRoll = i;
                if (h.equals("username"))
                    idxUser = i;
                if (h.equals("quiz"))
                    idxQuiz = i;
                if (h.equals("midterm") || h.equals("mid"))
                    idxMid = i;
                if (h.equals("endsem") || h.equals("end_sem") || h.equals("end sem") || h.equals("end"))
                    idxEnd = i;
            }

            String line;
            while ((line = br.readLine()) != null) {
                java.util.List<String> parts = parseCsvLine(line);
                String roll = idxRoll != -1 && idxRoll < parts.size() ? parts.get(idxRoll).trim() : "";
                String uname = idxUser != -1 && idxUser < parts.size() ? parts.get(idxUser).trim() : "";
                String quizS = idxQuiz != -1 && idxQuiz < parts.size() ? parts.get(idxQuiz).trim() : "";
                String midS = idxMid != -1 && idxMid < parts.size() ? parts.get(idxMid).trim() : "";
                String endS = idxEnd != -1 && idxEnd < parts.size() ? parts.get(idxEnd).trim() : "";

                String studentIdStr = "";
                String enrolled = "NO";
                String err = "";

                Integer studentUserId = null;
                if (!roll.isEmpty()) {
                    try (Connection c = ErpDb.get();
                            PreparedStatement ps = c
                                    .prepareStatement("SELECT user_id FROM students WHERE roll_no = ?")) {
                        ps.setString(1, roll);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                studentUserId = rs.getInt(1);
                        }
                    }
                }
                if (studentUserId == null && !uname.isEmpty()) {
                    try (Connection c = AuthDb.get();
                            PreparedStatement ps = c
                                    .prepareStatement("SELECT user_id FROM users_auth WHERE username = ?")) {
                        ps.setString(1, uname);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                studentUserId = rs.getInt(1);
                        }
                    }
                }

                if (studentUserId == null) {
                    err = "student not found";
                } else {
                    studentIdStr = String.valueOf(studentUserId);
                    try (Connection c = ErpDb.get();
                            PreparedStatement ps = c.prepareStatement(
                                    "SELECT enrollment_id FROM enrollments WHERE student_user_id = ? AND section_id = ?")) {
                        ps.setInt(1, studentUserId);
                        ps.setInt(2, sectionId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                enrolled = "YES";
                        }
                    }
                }

                out.add(new String[] { roll, uname, studentIdStr, enrolled, quizS, midS, endS, err });
            }
        }
        return out;
    }
}
