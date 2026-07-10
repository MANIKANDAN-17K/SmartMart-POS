package com.supermarketpos.dao;

import com.supermarketpos.model.AuditLog;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {

    public void insert(AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_log (username, action, details, timestamp) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());
            ps.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));
            ps.executeUpdate();
        }
    }

    public List<AuditLog> search(String username, String action, LocalDate date) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (username != null && !username.isBlank()) {
            sql.append(" AND username = ?");
            params.add(username);
        }
        if (action != null && !action.isBlank()) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        if (date != null) {
            sql.append(" AND DATE(timestamp) = ?");
            params.add(Date.valueOf(date));
        }
        sql.append(" ORDER BY timestamp DESC");

        List<AuditLog> logs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setDetails(rs.getString("details"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    logs.add(log);
                }
            }
        }
        return logs;
    }
}