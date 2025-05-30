package org.gihdm.model;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Document {
    private static final Logger logger = LoggerFactory.getLogger(Document.class);
   
    private String id;  // Stores UUID as string
    private String title;
    private String cloudUrl;
    private String fileId;
    private String category;
    private String storageProvider;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private long fileSize;
    
    public Document() {
        logger.trace("New empty document instance created");
    }
    
    public Document(String title, String cloudUrl, String category, 
                  String storageProvider, String uploadedBy) {
        logger.debug("Creating new document - Title: '{}', Category: '{}', Provider: '{}', Uploader: '{}'", 
            truncate(title, 50), 
            category, 
            storageProvider, 
            maskEmail(uploadedBy));
        
        this.title = title;
        this.cloudUrl = cloudUrl;
        this.category = category;
        this.storageProvider = storageProvider;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = LocalDateTime.now();
    }

    public String getId() {
        logger.trace("Accessing document ID");
        return id;
    }

    public void setId(String id) {
        if (this.id != null && !this.id.equals(id)) {
            logger.debug("Changing document ID from '{}' to '{}'", 
                maskId(this.id), maskId(id));
        }
        this.id = id;
    }

    public String getTitle() {
        logger.trace("Accessing document title");
        return title;
    }

    public void setTitle(String title) {
        if (this.title != null && !this.title.equals(title)) {
            logger.debug("Renaming document from '{}' to '{}'", 
                truncate(this.title, 50), truncate(title, 50));
        }
        this.title = title;
    }

    public String getCloudUrl() {
        logger.trace("Accessing document URL");
        return cloudUrl;
    }

    public void setCloudUrl(String cloudUrl) {
        if (this.cloudUrl != null && !this.cloudUrl.equals(cloudUrl)) {
            logger.info("Document storage URL changed for '{}'", truncate(title, 50));
        }
        this.cloudUrl = cloudUrl;
    }

    public String getFileId() {
        logger.trace("Accessing document file ID");
        return fileId;
    }

    public void setFileId(String fileId) {
        if (this.fileId != null && !this.fileId.equals(fileId)) {
            logger.debug("Changing file ID reference for document '{}'", truncate(title, 50));
        }
        this.fileId = fileId;
    }

    public String getStorageProvider() {
        logger.trace("Accessing storage provider");
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        if (this.storageProvider != null && !this.storageProvider.equals(storageProvider)) {
            logger.info("Storage provider changed from '{}' to '{}' for document '{}'",
                this.storageProvider, storageProvider, truncate(title, 50));
        }
        this.storageProvider = storageProvider;
    }

    public String getUploadedBy() {
        logger.trace("Accessing uploader info");
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        if (this.uploadedBy != null && !this.uploadedBy.equals(uploadedBy)) {
            logger.warn("Uploader changed from '{}' to '{}' for document '{}'",
                maskEmail(this.uploadedBy), maskEmail(uploadedBy), truncate(title, 50));
        }
        this.uploadedBy = uploadedBy;
    }

    private String maskEmail(String email) {
        if (email == null) return "[null]";
        int atIndex = email.indexOf('@');
        return atIndex > 2 
            ? email.substring(0, 2) + "***@" + email.substring(atIndex + 1)
            : "***@" + (atIndex > 0 ? email.substring(atIndex + 1) : "***");
    }

    private String maskId(String id) {
        return id != null && id.length() > 8 
            ? id.substring(0, 4) + "..." + id.substring(id.length() - 4)
            : "***";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "[null]";
        return text.length() > maxLength 
            ? text.substring(0, maxLength - 3) + "..." 
            : text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getType() {
        if (storageProvider != null && storageProvider.equalsIgnoreCase("CLOUDINARY")) {
            return "image";
        }
        return "document"; 
    }
    
    @Override
    public String toString() {
        return "Document{" +
               "title='" + truncate(title, 20) + '\'' +
               ", category='" + category + '\'' +
               ", provider='" + storageProvider + '\'' +
               ", uploadedBy='" + maskEmail(uploadedBy) + '\'' +
               '}';
    }
}