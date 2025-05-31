package org.gihdm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class HikariCPDataSource {
    private static final HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(HikariCPDataSource.class);

    static {
        try {
            // Explicitly register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Load configuration from environment variables first
            String dbUrl = System.getenv("DB_URL"); // Render's format: postgresql://user:pass@host/dbname
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            
            // If environment variables not set, fall back to properties file
            if (dbUrl == null || dbUser == null || dbPassword == null) {
                logger.info("Loading database config from application.properties");
                Properties props = new Properties();
                try (InputStream input = HikariCPDataSource.class.getResourceAsStream("/application.properties")) {
                    props.load(input);
                    dbUrl = props.getProperty("db.url");
                    dbUser = props.getProperty("db.user");
                    dbPassword = props.getProperty("db.password");
                }
            }

            // Parse Render's connection string
            String jdbcUrl;
            if (dbUrl.startsWith("postgresql://")) {
                // Convert Render format to JDBC format
                String cleanUrl = dbUrl.replace("postgresql://", "");
                int atIndex = cleanUrl.indexOf("@");
                String credentials = cleanUrl.substring(0, atIndex);
                String hostAndDb = cleanUrl.substring(atIndex + 1);
                
                // Split host:port/dbname (Render uses default port 5432)
                String host;
                String dbName;
                if (hostAndDb.contains("/")) {
                    int slashIndex = hostAndDb.indexOf("/");
                    host = hostAndDb.substring(0, slashIndex);
                    dbName = hostAndDb.substring(slashIndex + 1);
                } else {
                    host = hostAndDb;
                    dbName = "gracedm_prod_1kh6"; // default database
                }
                
                // Construct proper JDBC URL
                jdbcUrl = String.format("jdbc:postgresql://%s/%s?ssl=true&sslmode=require", host, dbName);
                
                // Extract username/password if not set separately
                if (dbUser == null || dbPassword == null) {
                    int colonIndex = credentials.indexOf(":");
                    dbUser = credentials.substring(0, colonIndex);
                    dbPassword = credentials.substring(colonIndex + 1);
                }
            } else {
                // Assume it's already a JDBC URL
                jdbcUrl = dbUrl;
            }

            logger.info("Using JDBC URL: " + jdbcUrl.replaceAll("password=[^&]*", "password=*****"));
            logger.info("Using username: " + dbUser);

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            
            // Optimized pool settings
            config.setPoolName("GraceDMPool");
            config.setMaximumPoolSize(Integer.parseInt(System.getenv().getOrDefault("DB_MAX_POOL_SIZE", "3")));
            config.setMinimumIdle(Integer.parseInt(System.getenv().getOrDefault("DB_MIN_IDLE", "1")));
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(5000);
            
            // PostgreSQL-specific optimizations
            config.addDataSourceProperty("preparedStatementCacheQueries", "256");
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            
            // Test the connection
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Successfully connected to database: " + conn.getMetaData().getDatabaseProductVersion());
            }
            
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