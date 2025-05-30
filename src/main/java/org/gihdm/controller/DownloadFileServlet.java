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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

@WebServlet("/download-file")
public class DownloadFileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DownloadFileServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileId = req.getParameter("id");
        if (fileId == null || fileId.trim().isEmpty()) {
            resp.sendError(400, "File ID is required");
            return;
        }

        try (DocumentRepository repo = new DocumentRepository()) {
        	repo.beginTransaction();
            Document doc = repo.findById(fileId);
            if (doc == null) {
                resp.sendError(404, "File not found");
                return;
            }

            String fileName = doc.getTitle();
            String contentType = getContentType(fileName);

            resp.setContentType(contentType);
            resp.setHeader("Content-Disposition", 
                "attachment; filename=\"" + fileName + "\"");

            try (InputStream fileStream = getFileStream(doc, req.getSession());
                 OutputStream out = resp.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
             repo.commit();
            logger.info("File downloaded successfully: {}", fileName);
        } catch (Exception e) {
            logger.error("Download failed for file ID: {}. Error: {}", fileId, e.getMessage(), e);
            resp.sendError(500, "Failed to download file");
        }
    }
    
    private InputStream getFileStream(Document doc, HttpSession session) throws IOException {
        return switch (doc.getStorageProvider()) {
            case "CLOUDINARY" -> CloudinaryService.getFileStream(doc.getFileId(), getFileExtension(doc.getTitle()));
            case "DRIVE" -> GoogleDriveService.getFileStream(doc.getFileId(), session);
            default -> new URL(doc.getCloudUrl()).openStream();
        };
    }


    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index != -1 && index < fileName.length() - 1) ? fileName.substring(index + 1) : "";
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) return "application/msword";
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}