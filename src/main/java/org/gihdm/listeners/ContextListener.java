package org.gihdm.listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.gihdm.config.HikariCPDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application context initialized");
        try {
            // Test database connection on startup
            HikariCPDataSource.getConnection().close();
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application context shutdown initiated");
        try {
            HikariCPDataSource.shutdown();
            logger.info("Database connection pool shutdown completed");
        } catch (Exception e) {
            logger.error("Error during database connection pool shutdown", e);
        }
        logger.info("Application context shutdown completed");
    }
}