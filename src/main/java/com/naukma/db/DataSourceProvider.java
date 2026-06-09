package com.naukma.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSourceProvider {

    private static HikariDataSource dataSource;

    private DataSourceProvider() {}

    public static void init(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        dataSource = new HikariDataSource(config);

        try {
            executeSchema();
            System.out.println("Schema successfully applied.");
        } catch (Exception e) {
            System.err.println("Failed to apply schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSchema() throws SQLException, IOException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized");
        }
        try (InputStream is = DataSourceProvider.class.getClassLoader()
                .getResourceAsStream("schema.sql");
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            if (is == null) {
                System.err.println("Warning: schema.sql not found in resources. Trying absolute path...");
                return;
            }

            String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (String statement : schema.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--"))
                    stmt.executeUpdate(trimmed);
            }

            System.out.println("Tables created successfully.");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new IllegalStateException("DataSource not initialized. Call DataSourceProvider.init() first.");
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }
}