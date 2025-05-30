package org.gihdm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.gihdm.services.*;
import org.gihdm.utils.FileTypeUtil;
import org.gihdm.repository.DocumentRepository;
import org.gihdm.security.CSRFTokenManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

@WebServlet("/upload")
@MultipartConfig
public class FileRouterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(FileRouterServlet.class);

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
    	    throws ServletException, IOException {
    	    
    	    HttpSession session = req.getSession();
    	    if (!validateRequest(session, req, resp)) {
    	        return;
    	    }

    	    String fileName = null;
    	    String fileUrl = null;
    	    String fileId = null;
    	    String storageProvider = null;
    	    String category = getValidCategory(req.getParameter("category"), req);
    	    String userEmail = (String) session.getAttribute("googleUserEmail");

    	    try {
    	        Part filePart = req.getPart("file");
    	        fileName = filePart.getSubmittedFileName();

    	        if (!validateFileSize(filePart, session, req, resp)) return;

    	        // Process upload
    	        UploadResult result = processUpload(filePart, category, session, req, resp);
    	        if (result == null) return;

    	        fileUrl = result.fileUrl();
    	        fileId = result.fileId();
    	        storageProvider = result.storageProvider();

    	        // Save to database
    	        saveDocumentMetadata(fileName, fileUrl, fileId, category, storageProvider, userEmail);

    	        // Success
    	        session.setAttribute("toastMessage", fileName + " uploaded successfully");
    	        resp.sendRedirect(req.getContextPath() + "/home?category=" + URLEncoder.encode(category, "UTF-8"));
    	        
    	    } catch (Exception e) {
    	        logger.error("Upload failed", e);
    	        if (!resp.isCommitted()) {
    	            session.setAttribute("toastMessage", "Upload failed: " + e.getMessage());
    	            session.setAttribute("toastType", "error");
    	            resp.sendRedirect(req.getContextPath() + "/home");
    	        }
    	    }
    	}

    private boolean validateRequest(HttpSession session, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (session.getAttribute("googleToken") == null || session.getAttribute("googleUserEmail") == null) {
            resp.sendRedirect("index.jsp");
            return false;
        }

        String csrfToken = req.getParameter("csrfToken");
        if (!CSRFTokenManager.isValidToken(session, csrfToken)) {
            resp.sendError(403, "Invalid CSRF Token");
            return false;
        }
        return true;
    }

    private String getValidCategory(String requestedCategory, HttpServletRequest req) {
        String[] validCategories = {
            "Letters", "Certificates", "Program Sheets",
            "Sermons", "Service Moments", "Events",
            "Services", "Programs", "Others"
        };

        if (requestedCategory == null || requestedCategory.trim().isEmpty()) {
            requestedCategory = (String) req.getSession().getAttribute("currentCategory");
        }

        if (requestedCategory == null) return "Letters";

        String normalizedCategory = requestedCategory;
        for (String valid : validCategories) {
            if (valid.equals(normalizedCategory)) return valid;
        }

        return "Letters"; 
    }

    private boolean validateFileSize(Part filePart, HttpSession session, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long maxSize = 50 * 1024 * 1024;
        if (filePart.getSize() > maxSize) {
            session.setAttribute("toastMessage", "File too large (max 50MB)");
            resp.sendRedirect(req.getContextPath() + "/home");
            return false;
        }
        return true;
    }

    private UploadResult processUpload(Part filePart, String category,
                                       HttpSession session, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String fileType = FileTypeUtil.getFileType(filePart.getSubmittedFileName());

        if (FileTypeUtil.isImage(fileType)) {
            String uploadedUrl = CloudinaryService.upload(filePart);
            String publicId = CloudinaryService.extractPublicId(uploadedUrl);
            return new UploadResult(uploadedUrl, publicId, "CLOUDINARY");
        } else if (FileTypeUtil.isVideo(fileType)) {
            session.setAttribute("toastMessage", "Uploads for Videos are temporarily unavailable.");
            session.setAttribute("toastType", "error");
            resp.sendRedirect(req.getContextPath() + "/home?category=" + URLEncoder.encode(category, "UTF-8"));
            return null;
        } else {
            var driveResult = GoogleDriveService.upload(filePart, category, session);
            return new UploadResult(driveResult.fileUrl(), driveResult.fileId(), "DRIVE");
        }
    }

    private void saveDocumentMetadata(String fileName, String fileUrl, String fileId,
                                      String category, String storageProvider, String uploadedBy) throws SQLException {
        try (DocumentRepository repo = new DocumentRepository()) {
        	repo.beginTransaction();
            repo.save(fileName, fileUrl, fileId, category, storageProvider, uploadedBy);
            repo.commit();
        }
    }

    private void handleUploadFailure(Exception e, String fileUrl, String fileId, String storageProvider,
            HttpSession session, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
logger.error("File upload failed", e);

// Only cleanup if we have both fileUrl and fileId
if (fileUrl != null && fileId != null && storageProvider != null) {
cleanupFailedUpload(fileUrl, fileId, storageProvider, session);
}

session.setAttribute("toastMessage", "Upload failed: " + e.getMessage());
session.setAttribute("toastType", "error");
resp.sendRedirect(req.getContextPath() + "/home");
}

private void cleanupFailedUpload(String fileUrl, String fileId, String storageProvider, 
            HttpSession session) {
try {
logger.debug("Attempting cleanup of {} file: {}", storageProvider, fileId);
switch (storageProvider) {
case "CLOUDINARY" -> {
String publicId = CloudinaryService.extractPublicId(fileUrl);
if (publicId != null) {
 CloudinaryService.delete(publicId);
}
}
case "DRIVE" -> {
// Use fileId directly if available, otherwise extract from URL
String idToDelete = (fileId != null) ? fileId : GoogleDriveService.extractFileId(fileUrl);
if (idToDelete != null) {
 GoogleDriveService.delete(idToDelete, session);
}
}
}
} catch (Exception ex) {
logger.error("Cleanup failed for {}", fileUrl, ex);
}
}

    private record UploadResult(String fileUrl, String fileId, String storageProvider) {}
}
