package org.gihdm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HikariCPDataSource {
    private static final HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(HikariCPDataSource.class);

    static {
        try {
            // 1. Load driver (essential for Render)
            Class.forName("org.postgresql.Driver");
            
            // 2. Configure Hikari with free-tier optimized settings
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://dpg-d0s747c9c44c73crpqog-a/gracedm_prod_1kh6?ssl=true&sslmode=require");
            config.setUsername("gracedm_admin");
            config.setPassword("0SicEutGPr0N6lLRzXWlKBe0iIkocayT");
            
            // Free-tier optimized pool settings
            config.setMaximumPoolSize(2);  // Very conservative for free tier
            config.setMinimumIdle(1);      // Keep just 1 connection when idle
            config.setConnectionTimeout(30000); // 30 seconds
            config.setIdleTimeout(60000); // 1 minute idle timeout
            config.setMaxLifetime(180000); // 3 minutes max lifetime
            
            // PostgreSQL optimizations that save resources
            config.addDataSourceProperty("preparedStatementCacheQueries", "0"); // Disable cache
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", "0"); // Disable cache
            
            dataSource = new HikariDataSource(config);
            logger.info("Database pool started with free-tier optimized settings");
            
        } catch (Exception e) {
            logger.error("Database pool initialization failed", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool shutdown complete");
        }
    }
    		
	public static HikariDataSource getDataSource() {
		return dataSource;
	}
}