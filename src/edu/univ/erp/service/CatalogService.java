package edu.univ.erp.service;

import edu.univ.erp.data.ErpDb;
import edu.univ.erp.domain.*;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class CatalogService {

    public DefaultTableModel listCatalog() throws SQLException {
        String[] cols = { "Section ID", "Code", "Title", "Credits", "Day/Time", "Room",
                "Capacity", "Enrolled", "Available", "Instructor" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT s.section_id, c.code, c.title, c.credits, s.day_time, s.room, " +
                "s.capacity, COALESCE(COUNT(e.enrollment_id), 0) AS enrolled, " +
                "(s.capacity - COALESCE(COUNT(e.enrollment_id), 0)) AS available, " +
                "s.instructor_user_id AS instructor_id " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "GROUP BY s.section_id, c.code, c.title, c.credits, s.day_time, s.room, " +
                "         s.capacity, s.instructor_user_id " +
                "ORDER BY c.code, s.day_time";

        List<Object[]> rows = new ArrayList<>();
        Set<Integer> instrIds = new HashSet<>();
        try (Connection c = ErpDb.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Integer instr = (Integer) rs.getObject(10);
                if (instr != null)
                    instrIds.add(instr);
                rows.add(new Object[] {
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getInt(9),
                        instr
                });
            }
        }

        Map<Integer, String> names = new HashMap<>();
        try {
            names = edu.univ.erp.data.AuthLookup.usernamesForIds(instrIds);
        } catch (SQLException e) {
            System.err.println("Auth lookup failed: " + e.getMessage());
        }

        for (Object[] r : rows) {
            Integer instr = (Integer) r[9];
            String uname = instr != null ? names.getOrDefault(instr, "Unassigned") : "Unassigned";
            m.addRow(new Object[] { r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], uname });
        }

        String sqlNoSections = "SELECT course_id, code, title, credits FROM courses WHERE course_id NOT IN (SELECT DISTINCT course_id FROM sections) ORDER BY code";
        try (Connection c = ErpDb.get();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sqlNoSections)) {
            while (rs.next()) {
                m.addRow(new Object[] { "", rs.getString(2), rs.getString(3), rs.getInt(4), "", "", 0, 0, 0,
                        "Unassigned" });
            }
        }
        return m;
    }

    public DefaultTableModel searchCatalog(String searchTerm) throws SQLException {
        String[] cols = { "Section ID", "Code", "Title", "Credits", "Day/Time", "Room",
                "Capacity", "Enrolled", "Available", "Instructor" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT s.section_id, c.code, c.title, c.credits, s.day_time, s.room, " +
                "s.capacity, COALESCE(COUNT(e.enrollment_id), 0) AS enrolled, " +
                "(s.capacity - COALESCE(COUNT(e.enrollment_id), 0)) AS available, " +
                "COALESCE(inst_auth.username, 'Unassigned') AS instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "LEFT JOIN users_auth inst_auth ON s.instructor_user_id = inst_auth.user_id " +
                "WHERE UPPER(c.code) LIKE ? OR UPPER(c.title) LIKE ? " +
                "GROUP BY s.section_id, c.code, c.title, c.credits, s.day_time, s.room, " +
                "         s.capacity, inst_auth.username " +
                "ORDER BY c.code, s.day_time";

        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement(sql)) {
            String param = "%" + searchTerm.toUpperCase() + "%";
            ps.setString(1, param);
            ps.setString(2, param);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.addRow(new Object[] {
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getInt(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getInt(8),
                            rs.getInt(9),
                            rs.getString(10)
                    });
                }
            }
        }
        return m;
    }

    public int getAvailableSeats(int sectionId) throws SQLException {
        String sql = "SELECT s.capacity - COALESCE(COUNT(e.enrollment_id), 0) " +
                "FROM sections s " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE s.section_id = ? " +
                "GROUP BY s.capacity";

        try (Connection c = ErpDb.get();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY code";

        try (Connection c = ErpDb.get();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(new Course(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4)));
            }
        }
        return courses;
    }
}
