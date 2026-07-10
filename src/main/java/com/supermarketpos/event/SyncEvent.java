package com.supermarketpos.event;

public class SyncEvent {
    public enum Type { STARTED, PROGRESS, SUCCESS, FAILED }

    private final Type type;
    private final String syncCategory;
    private final String message;

    public SyncEvent(Type type, String syncCategory, String message) {
        this.type = type;
        this.syncCategory = syncCategory;
        this.message = message;
    }

    public Type getType() { return type; }
    public String getSyncCategory() { return syncCategory; }
    public String getMessage() { return message; }
}