package com.supermarketpos.dao;

import com.supermarketpos.model.AuditLog;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {

    public void insert(AuditLog log) throws SQLException {
        if (log == null) return;
        String sql = "INSERT INTO audit_log (username, action, details, timestamp) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, log.getUsername() != null ? log.getUsername() : "System");
            ps.setString(2, log.getAction() != null ? log.getAction() : "ACTION");
            ps.setString(3, log.getDetails() != null ? log.getDetails() : "");
            LocalDateTime ts = log.getTimestamp() != null ? log.getTimestamp() : LocalDateTime.now();
            ps.setTimestamp(4, Timestamp.valueOf(ts));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log insert failed: " + e.getMessage());
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
                    Timestamp ts = rs.getTimestamp("timestamp");
                    log.setTimestamp(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Audit log search failed: " + e.getMessage());
            return new ArrayList<>();
        }
        return logs;
    }
}