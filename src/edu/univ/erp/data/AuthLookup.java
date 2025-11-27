package edu.univ.erp.data;

import java.sql.*;
import java.util.*;

public class AuthLookup {
    public static Map<Integer, String> usernamesForIds(Set<Integer> ids) throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        if (ids == null || ids.isEmpty())
            return map;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT user_id, username FROM users_auth WHERE user_id IN (");
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < ids.size(); i++)
            sj.add("?");
        sb.append(sj.toString()).append(")");

        try (Connection c = AuthDb.get(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Integer id : ids)
                ps.setInt(idx++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt(1), rs.getString(2));
                }
            }
        }
        return map;
    }
}
