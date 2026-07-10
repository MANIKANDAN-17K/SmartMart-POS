package com.supermarketpos.database;

import com.supermarketpos.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionPool {

    private static volatile HikariDataSource dataSource;

    private ConnectionPool() {
    }

    public static synchronized void initialize() {
        if (dataSource != null) {
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.getJdbcUrl());
        config.setUsername(DatabaseConfig.getUsername());
        config.setPassword(DatabaseConfig.getPassword());
        config.setMaximumPoolSize(DatabaseConfig.getMaximumPoolSize());
        config.setMinimumIdle(DatabaseConfig.getMinimumIdle());
        config.setConnectionTimeout(DatabaseConfig.getConnectionTimeout());
        config.setIdleTimeout(DatabaseConfig.getIdleTimeout());
        config.setMaxLifetime(DatabaseConfig.getMaxLifetime());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
