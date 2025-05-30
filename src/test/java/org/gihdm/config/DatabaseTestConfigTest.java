package org.gihdm.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTestConfigTest {
    static {
        try {
            // Register H2 driver (optional for newer JDBC versions)
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", 
            "sa", 
            ""
        );
    }
}