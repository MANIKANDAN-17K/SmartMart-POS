package com.supermarketpos.db;

import com.supermarketpos.database.DatabaseInitializer;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseManager {
    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DatabaseInitializer.getConnection();
    }
}
