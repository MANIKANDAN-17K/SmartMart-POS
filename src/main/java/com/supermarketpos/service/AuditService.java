package com.supermarketpos.service;

import com.supermarketpos.dao.AuditLogDao;
import com.supermarketpos.model.AuditLog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AuditService {

    private final AuditLogDao auditLogDao = new AuditLogDao();

    public void log(String username, String action, String details) {
        try {
            auditLogDao.insert(new AuditLog(username, action, details));
        } catch (SQLException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> getAuditLogs(String username, String action, LocalDate date) throws SQLException {
        return auditLogDao.search(username, action, date);
    }
}