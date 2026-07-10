package com.supermarketpos.database;

import com.supermarketpos.config.DatabaseConfig;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Single entry point every DAO uses to get a connection. Runs Flyway migrations
 * (src/main/resources/sql/migrations) exactly once, lazily, on first use — so it
 * works whether or not MainApp explicitly calls initialize() at startup.
 */
public final class DatabaseInitializer {

    private static volatile boolean migrated = false;

    private DatabaseInitializer() {
    }

    public static synchronized void initialize() {
        ConnectionPool.initialize();
        if (!migrated) {
            runMigrations();
            migrated = true;
        }
    }

    private static void runMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConfig.getJdbcUrl(), DatabaseConfig.getUsername(), DatabaseConfig.getPassword())
                .locations("classpath:sql/migrations")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }

    public static Connection getConnection() throws SQLException {
        if (!migrated) {
            initialize();
        }
        return ConnectionPool.getConnection();
    }

    public static void shutdown() {
        ConnectionPool.shutdown();
    }
}
