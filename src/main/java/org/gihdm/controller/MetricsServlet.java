package org.gihdm.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import org.gihdm.config.HikariCPDataSource;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariDataSource;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(MetricsServlet.class);
    
    private static final String AUTH_TOKEN = Optional.ofNullable(System.getenv("METRICS_TOKEN"))
        .orElseGet(() -> {
            String generatedToken = generateSecureToken();
            logger.warn("METRICS_TOKEN not set - Using generated token: {}", generatedToken);
            return generatedToken;
        });
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Authentication check
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.equals(AUTH_TOKEN)) {
                logger.warn("Unauthorized access attempt from IP: {}", req.getRemoteAddr());
                resp.setHeader("WWW-Authenticate", "Bearer");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // Metrics collection
            HikariDataSource ds = (HikariDataSource)HikariCPDataSource.getDataSource();
            Runtime runtime = Runtime.getRuntime();
            
            logger.debug("DB Pool Metrics - Active:{}, Idle:{}, Total:{}", 
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections());
            
            // Response with both DB and memory metrics
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(String.format(
                "{\"db\":{" +
                "\"active\":%d, " +
                "\"idle\":%d, " +
                "\"total\":%d" +
                "}, " +
                "\"memory\":{" +
                "\"used_mb\":%.2f, " +
                "\"free_mb\":%.2f, " +
                "\"total_mb\":%.2f, " +
                "\"max_mb\":%.2f" +
                "}}",
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections(),
                bytesToMb(runtime.totalMemory() - runtime.freeMemory()),
                bytesToMb(runtime.freeMemory()),
                bytesToMb(runtime.totalMemory()),
                bytesToMb(runtime.maxMemory())
            ));
            
        } catch (Exception e) {
            logger.error("Metrics endpoint failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static String generateSecureToken() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        } catch (Exception e) {
            logger.error("Failed to generate secure token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }
    
    private static double bytesToMb(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
    
    @Override
    public void init() {
        if (System.getenv("METRICS_TOKEN") == null) {
            logger.warn("⚠️ METRICS_TOKEN environment variable not set - Using generated token");
        }
        logger.info("Metrics endpoint requires Authorization header with token");
    }
}