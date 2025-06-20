 package org.gihdm.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.gihdm.config.GoogleAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.gihdm.model.UploadResult;

public class GoogleDriveService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final Map<String, String> FOLDER_CACHE = new HashMap<>();
    
    // Timeout configurations (in milliseconds)
    private static final int CONNECT_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);
    private static final int READ_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);
    private static final int WRITE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    public static UploadResult upload(Part filePart, String category, HttpSession session) throws IOException {
        String fileName = filePart.getSubmittedFileName();
        Credential credential = (Credential) session.getAttribute("googleCredential");
        
        if (credential == null) {
            throw new IOException("No Google credential found in session");
        }

        // Configure Drive service with timeouts
        Drive drive = createDriveServiceWithTimeouts(credential);

        // Get MIME type
        String mimeType = getMimeType(filePart.getContentType(), fileName);

        // Prepare file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setDescription("Uploaded from GraceDM - Category: " + category);
        
        // Get or create folder ID
        String folderId = getFolderIdWithRetry(category, drive);
        fileMetadata.setParents(Collections.singletonList(folderId));

        // Prepare content
        InputStreamContent content = new InputStreamContent(mimeType, filePart.getInputStream());
        content.setLength(filePart.getSize());

        // Upload with retry logic
        File uploadedFile = uploadWithRetry(drive, fileMetadata, content);

        // Set public permissions
        setPublicPermission(drive, uploadedFile.getId());

        // Generate view URL
        String viewUrl = uploadedFile.getWebViewLink() != null ? 
                       uploadedFile.getWebViewLink() : 
                       "https://drive.google.com/file/d/" + uploadedFile.getId() + "/view";

        return new UploadResult(viewUrl, uploadedFile.getId(), "DRIVE");
    }

    private static Drive createDriveServiceWithTimeouts(Credential credential) {
        return new Drive.Builder(
                GoogleAuthConfig.getHttpTransport(),
                GoogleAuthConfig.getJsonFactory(),
                new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) throws IOException {
                        credential.initialize(request);
                        request.setConnectTimeout(CONNECT_TIMEOUT);
                        request.setReadTimeout(READ_TIMEOUT);
                        request.setWriteTimeout(WRITE_TIMEOUT);
                    }
                })
                .setApplicationName("GraceDM")
                .build();
    }

    private static File uploadWithRetry(Drive drive, File fileMetadata, InputStreamContent content) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.debug("Attempting upload (attempt {}/{})", attempt, MAX_RETRIES);
                return drive.files()
                        .create(fileMetadata, content)
                        .setFields("id,webViewLink,webContentLink")
                        .execute();
            } catch (IOException e) {
                lastException = e;
                logger.warn("Upload attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Upload interrupted", ie);
                    }
                }
            }
        }
        
        throw new IOException("Failed to upload after " + MAX_RETRIES + " attempts", lastException);
    }

    private static String getFolderIdWithRetry(String category, Drive drive) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (FOLDER_CACHE.containsKey(category)) {
                    return FOLDER_CACHE.get(category);
                }
                
                String folderName = "ChurchDocs_" + category;
                FileList list = drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false")
                    .setFields("files(id)").execute();

                if (!list.getFiles().isEmpty()) {
                    String id = list.getFiles().get(0).getId();
                    FOLDER_CACHE.put(category, id);
                    return id;
                }

                // Create folder if it doesn't exist
                File folder = new File();
                folder.setName(folderName);
                folder.setMimeType("application/vnd.google-apps.folder");
                File created = drive.files().create(folder).setFields("id").execute();

                FOLDER_CACHE.put(category, created.getId());
                return created.getId();
                
            } catch (IOException e) {
                lastException = e;
                logger.warn("Folder operation attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Folder operation interrupted", ie);
                    }
                }
            }
        }
        
        throw new IOException("Failed to get/create folder after " + MAX_RETRIES + " attempts", lastException);
    }

    private static String getMimeType(String originalMimeType, String fileName) {
        if (originalMimeType != null && !originalMimeType.startsWith("application/octet-stream")) {
            return originalMimeType;
        }
        
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt": return "text/plain";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            default: return "application/octet-stream";
        }
    }

    private static void setPublicPermission(Drive drive, String fileId) throws IOException {
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        
        // Use batch request for better performance when setting multiple permissions
        BatchRequest batch = drive.batch();
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onSuccess(Permission permission, HttpHeaders responseHeaders) {
                logger.debug("Successfully set public permission for file: {}", fileId);
            }

            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                logger.warn("Failed to set public permission for file {}: {}", fileId, e.getMessage());
            }
        };

        drive.permissions().create(fileId, permission)
            .setFields("id")
            .queue(batch, callback);
        
        try {
            batch.execute();
        } catch (IOException e) {
            logger.error("Error executing batch permission request", e);
            throw e;
        }
    }

    public static void delete(String fileId, HttpSession session) throws IOException {
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("File ID cannot be null or empty");
        }

        Credential credential = (Credential) session.getAttribute("googleCredential");
        if (credential == null) {
            throw new IOException("No Google credential found in session");
        }

        Drive drive = createDriveServiceWithTimeouts(credential);
        
        try {
            drive.files().delete(fileId).execute();
            logger.info("Successfully deleted file: {}", fileId);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                logger.warn("File not found, may have already been deleted: {}", fileId);
                return;
            }
            throw e;
        }
    }

    public static InputStream getFileStream(String fileId, HttpSession session) throws IOException {
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("File ID cannot be null or empty");
        }

        Credential credential = (Credential) session.getAttribute("googleCredential");
        if (credential == null) {
            throw new IOException("No Google credential found in session");
        }

        Drive drive = createDriveServiceWithTimeouts(credential);

        try {
            File file = drive.files().get(fileId).setFields("mimeType,exportLinks").execute();

            if (file.getMimeType().startsWith("application/vnd.google-apps.")) {
                return drive.files().export(fileId, getExportMimeType(file.getMimeType()))
                        .executeMediaAsInputStream();
            }

            return drive.files().get(fileId).executeMediaAsInputStream();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new IOException("File not found: " + fileId, e);
            }
            throw e;
        }
    }

    public static String extractFileId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (url.contains("/d/")) {
            int start = url.indexOf("/d/") + 3;
            int end = url.indexOf('/', start);
            if (end == -1) end = url.indexOf('?', start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        } else if (url.contains("id=")) {
            int start = url.indexOf("id=") + 3;
            int end = url.indexOf('&', start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        }
        return null;
    }

    private static String getExportMimeType(String type) {
        if (type == null) {
            return "application/pdf";
        }

        return switch (type) {
            case "application/vnd.google-apps.document" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "application/vnd.google-apps.spreadsheet" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "application/vnd.google-apps.presentation" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "application/vnd.google-apps.drawing" -> "image/png";
            default -> "application/pdf";
        };
    }
}