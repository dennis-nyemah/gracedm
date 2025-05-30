package org.gihdm.exceptions;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/error-handler")
public class ExceptionHandler extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleError(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleError(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Throwable exception = (Throwable) req.getAttribute("jakarta.servlet.error.exception");
        int statusCode = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
        String requestUri = (String) req.getAttribute("jakarta.servlet.error.request_uri");
        
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
        
        // Log the error with appropriate level
        if (statusCode >= 500) {
            logger.error("Server Error {} at {}: {}", statusCode, requestUri, errorMessage, exception);
        } else if (statusCode >= 400) {
            logger.warn("Client Error {} at {}: {}", statusCode, requestUri, errorMessage);
        } else {
            logger.info("Error {} at {}: {}", statusCode, requestUri, errorMessage);
        }

        // Send JSON response
        resp.setContentType("application/json");
        resp.setStatus(statusCode);
        resp.getWriter().printf(
            "{\"status\":%d,\"message\":\"%s\"}", 
            statusCode, errorMessage
        );
        
        logger.debug("Error response sent for URI: {}", requestUri);
    }
}    