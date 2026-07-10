package com.supermarketpos.dao;

import com.supermarketpos.model.SyncLog;
import com.supermarketpos.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SyncLogDao {

    public void insert(SyncLog log) throws SQLException {
        String sql = "INSERT INTO sync_log (sync_type, status, message, timestamp) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, log.getSyncType());
            ps.setString(2, log.getStatus());
            ps.setString(3, log.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));
            ps.executeUpdate();
        }
    }

    public List<SyncLog> getHistory() throws SQLException {
        String sql = "SELECT * FROM sync_log ORDER BY timestamp DESC LIMIT 100";
        List<SyncLog> logs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SyncLog log = new SyncLog();
                log.setId(rs.getInt("id"));
                log.setSyncType(rs.getString("sync_type"));
                log.setStatus(rs.getString("status"));
                log.setMessage(rs.getString("message"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                logs.add(log);
            }
        }
        return logs;
    }

    public SyncLog getLastSync() throws SQLException {
        String sql = "SELECT * FROM sync_log ORDER BY timestamp DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                SyncLog log = new SyncLog();
                log.setId(rs.getInt("id"));
                log.setSyncType(rs.getString("sync_type"));
                log.setStatus(rs.getString("status"));
                log.setMessage(rs.getString("message"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                return log;
            }
            return null;
        }
    }
}