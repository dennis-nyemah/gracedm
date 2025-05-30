package org.gihdm.controller;

import org.gihdm.repository.DocumentRepository;
import org.gihdm.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

public class HomeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(HomeServlet.class);
   
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String currentCategory = req.getParameter("category");
        if (currentCategory != null) {
        	req.getSession().setAttribute("currentCategory", currentCategory);
        }
        String user = session != null ? (String) session.getAttribute("googleUserEmail") : "anonymous";
        
        logger.info("Home page request from user: {}", user);
        
        try (DocumentRepository repo = new DocumentRepository()) {
        	repo.beginTransaction();
            logger.debug("Fetching documents for home page");
            List<Document> allDocuments = repo.findAll();
            Map<String, List<Document>> documentsByCategory = allDocuments.stream()
            	    .collect(Collectors.groupingBy(d -> d.getCategory())); 
            logger.debug("Fetched documents: " + documentsByCategory);
         
            req.setAttribute("documentsByCategory", documentsByCategory);
            req.setAttribute("currentCategory", currentCategory);
            
            if (session != null) {
                if (session.getAttribute("toastMessage") != null) {
                    req.setAttribute("toastMessage", session.getAttribute("toastMessage"));
                    req.setAttribute("toastType", session.getAttribute("toastType"));
                    session.removeAttribute("toastMessage");
                    session.removeAttribute("toastType");
                }
            }
            
            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
            repo.commit();
            logger.info("Successfully rendered home page for user: {}", user);
        } catch (SQLException e) {
            logger.error("Database error loading documents for user: {}. Error: {}", user, e.getMessage(), e);
            throw new ServletException("Database error", e);
        }
    }
}        