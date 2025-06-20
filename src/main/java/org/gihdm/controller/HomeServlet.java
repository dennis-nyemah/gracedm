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
            	    .collect(Collectors.groupingBy(Document::getCategory)); 
            logger.debug("Fetched documents: " + documentsByCategory);       
            

            // Create separate lists for media feeds based on actual categories
            List<Document> allVideos = allDocuments.stream()
                    .filter(d -> d.getCategory().equals("Videos") || 
                               d.getCategory().equals("Sermons") ||
                               d.getCategory().equals("Service Moments") ||
                               d.getCategory().equals("Events"))
                    .collect(Collectors.toList());
            
            List<Document> allPictures = allDocuments.stream()
                    .filter(d -> d.getCategory().equals("Pictures") || 
                               d.getCategory().equals("Services") ||
                               d.getCategory().equals("Programs") ||
                               d.getCategory().equals("Others"))
                    .collect(Collectors.toList());
            
            List<Document> allDocs = allDocuments.stream()
                    .filter(d -> d.getCategory().equals("Documents") || 
                               d.getCategory().equals("Letters") ||
                               d.getCategory().equals("Certificates") ||
                               d.getCategory().equals("Program Sheets"))
                    .collect(Collectors.toList());
            
           
         
            req.setAttribute("documentsByCategory", documentsByCategory);
            req.setAttribute("allVideos", allVideos);
            req.setAttribute("allPictures", allPictures);
            req.setAttribute("allDocuments", allDocs);
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