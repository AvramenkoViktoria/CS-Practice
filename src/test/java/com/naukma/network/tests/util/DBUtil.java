package com.naukma.network.tests.util;

import com.naukma.db.DataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    public static void clearDatabase() throws SQLException {
        try (Connection conn = DataSourceProvider.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM product_group_members");
            stmt.executeUpdate("DELETE FROM products");
            stmt.executeUpdate("DELETE FROM product_groups");

            System.out.println("Database cleared before test");
        }
    }
}
