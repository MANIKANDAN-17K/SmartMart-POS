package com.supermarketpos.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads src/main/resources/application.properties (on the classpath after build).
 * Note: config.properties at the project root is NOT on the classpath as-is —
 * if you want it to override secrets like db.password, move it under
 * src/main/resources/ or load it from a filesystem path explicitly.
 */
public final class DatabaseConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        load();
    }

    private DatabaseConfig() {
    }

    private static void load() {
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            PROPERTIES.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application.properties", e);
        }
    }

    public static String getHost() {
        return PROPERTIES.getProperty("db.host", "localhost");
    }

    public static int getPort() {
        return Integer.parseInt(PROPERTIES.getProperty("db.port", "3306"));
    }

    public static String getDatabaseName() {
        return PROPERTIES.getProperty("db.name");
    }

    public static String getUsername() {
        return PROPERTIES.getProperty("db.username");
    }

    public static String getPassword() {
        return PROPERTIES.getProperty("db.password");
    }

    public static int getMaximumPoolSize() {
        return Integer.parseInt(PROPERTIES.getProperty("db.pool.maximumPoolSize", "10"));
    }

    public static int getMinimumIdle() {
        return Integer.parseInt(PROPERTIES.getProperty("db.pool.minimumIdle", "5"));
    }

    public static long getConnectionTimeout() {
        return Long.parseLong(PROPERTIES.getProperty("db.pool.connectionTimeout", "30000"));
    }

    public static long getIdleTimeout() {
        return Long.parseLong(PROPERTIES.getProperty("db.pool.idleTimeout", "600000"));
    }

    public static long getMaxLifetime() {
        return Long.parseLong(PROPERTIES.getProperty("db.pool.maxLifetime", "1800000"));
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabaseName()
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }
}
