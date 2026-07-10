package com.supermarketpos.util;

import com.supermarketpos.database.DatabaseInitializer;
import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DatabaseInitializer.getConnection();
    }
}
