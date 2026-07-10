package com.supermarketpos.model;

import java.time.LocalDateTime;

public class SyncLog {
    private int id;
    private String syncType; // PRODUCTS, CUSTOMERS, PURCHASES, SALES, INVENTORY, ALL
    private String status;   // SUCCESS, FAILED
    private String message;
    private LocalDateTime timestamp;

    public SyncLog() {}

    public SyncLog(String syncType, String status, String message) {
        this.syncType = syncType;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSyncType() { return syncType; }
    public void setSyncType(String syncType) { this.syncType = syncType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}