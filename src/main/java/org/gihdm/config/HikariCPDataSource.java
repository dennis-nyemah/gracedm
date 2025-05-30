package org.gihdm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class HikariCPDataSource {
    private static final HikariDataSource dataSource;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(HikariCPDataSource.class);

    static {
        try {
        	Class.forName("org.postgresql.Driver");
            // Load configuration
            Properties props = new Properties();
            String jdbcUrl = System.getenv("DB_URL");
            String username = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            // Fallback to local properties if env vars not set
            if (jdbcUrl == null || username == null || password == null) {
                try (InputStream input = HikariCPDataSource.class
                        .getResourceAsStream("/application.properties")) {
                    props.load(input);
                    jdbcUrl = props.getProperty("db.url");
                    username = props.getProperty("db.user");
                    password = props.getProperty("db.password");
                }
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            
            // Production-optimized settings
            config.setMaximumPoolSize(Integer.parseInt( System.getenv().getOrDefault("DB_MAX_POOL_SIZE", "3"))); 
            config.setMinimumIdle(Integer.parseInt(System.getenv().getOrDefault("DB_MIN_IDLE", "1"))); 
            config.setConnectionTimeout(30000); 
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000); 
            config.setLeakDetectionThreshold(5000);
            config.setPoolName("GraceDMPool");

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize database pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
    	logger.debug("Acquiring DB connection from pool");
    	Connection conn = dataSource.getConnection();
    	logger.debug("Obtained connection [{}]", conn);
        return conn;
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