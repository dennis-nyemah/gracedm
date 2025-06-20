package org.gihdm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.gihdm.model.Document;
import org.gihdm.repository.DocumentRepository;
import org.gihdm.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/delete-file")
public class DeleteFileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DeleteFileServlet.class);

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String userEmail = (String) session.getAttribute("googleUserEmail");

        // Authentication check
        if (userEmail == null) {
            resp.sendError(401, "Unauthorized");
            return;
        }
        
        // Input validation
        String fileId = req.getParameter("id");
        if (fileId == null || fileId.trim().isEmpty()) {
            resp.sendError(400, "Invalid file ID");
            return;
        }

        try (DocumentRepository repo = new DocumentRepository()) {
        	repo.beginTransaction();
            Document doc = repo.findById(fileId);

            // Authorization check
            if (doc == null || !userEmail.equals(doc.getUploadedBy())) {
                resp.sendError(403, "Forbidden");
                return;
            }

            // Delete from storage provider
            switch (doc.getStorageProvider()) {
                case "CLOUDINARY" -> CloudinaryService.delete(doc.getFileId());
                case "DRIVE" -> GoogleDriveService.delete(doc.getFileId(), session);
                case "YOUTUBE" -> YouTubeService.delete(doc.getFileId(), req.getSession());
                default -> throw new IllegalArgumentException("Unknown storage provider");
            }
            
            
            // Delete metadata
            repo.delete(fileId);
            resp.setStatus(200);
            repo.commit();
        } catch (Exception e) {
            logger.error("Failed to delete file {} by user {}. Error: {}", 
                    fileId, userEmail, e.getMessage(), e);
            resp.sendError(500, "An internal error occurred while deleting the file.");
        }
    }
}
