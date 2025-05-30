package org.gihdm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/auth-check")
public class AuthCheckServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthCheckServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        HttpSession session = req.getSession(false); 
        
        if (session == null || session.getAttribute("googleToken") == null) {
            logger.debug("Auth check failed - no valid session or token");
            resp.setStatus(401);
        } else {
            String user = (String) session.getAttribute("googleUserEmail");
            logger.trace("Auth check passed for user: {}", user != null ? user : "[unknown]");
            resp.setStatus(200);
        }
    }
}