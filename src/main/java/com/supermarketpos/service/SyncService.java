package com.supermarketpos.service;

import com.supermarketpos.dao.SyncLogDao;
import com.supermarketpos.model.SyncLog;

import java.sql.SQLException;
import java.util.List;

public class SyncService {

    private final SyncLogDao syncLogDao = new SyncLogDao();

    public void recordLog(SyncLog log) {
        try {
            syncLogDao.insert(log);
        } catch (SQLException e) {
            // Logging failure must never crash the sync flow or affect local data
            System.err.println("Failed to write sync log: " + e.getMessage());
        }
    }

    public SyncLog getLastSync() throws SQLException {
        return syncLogDao.getLastSync();
    }

    public List<SyncLog> getSyncHistory() throws SQLException {
        return syncLogDao.getHistory();
    }
}