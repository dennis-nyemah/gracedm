package org.gihdm.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.gihdm.config.GoogleAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GoogleDriveService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final Map<String, String> FOLDER_CACHE = new HashMap<>();

    public static UploadResult upload(Part filePart, String category, HttpSession session) throws IOException {
        String fileName = filePart.getSubmittedFileName();
        Credential credential = (Credential) session.getAttribute("googleCredential");
        Drive drive = GoogleAuthConfig.getDriveService(credential);

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(getFolderId(category, drive)));

        InputStreamContent content = new InputStreamContent(filePart.getContentType(), filePart.getInputStream());

        File uploaded = drive.files().create(fileMetadata, content)
            .setFields("id,webViewLink")
            .execute();

        return new UploadResult(uploaded.getWebViewLink(), uploaded.getId(), "DRIVE");
    }

    public static void delete(String fileId, HttpSession session) throws IOException {
        Credential credential = (Credential) session.getAttribute("googleCredential");
        Drive drive = GoogleAuthConfig.getDriveService(credential);
        drive.files().delete(fileId).execute();
    }

    public static InputStream getFileStream(String fileId, HttpSession session) throws IOException {
        Credential credential = (Credential) session.getAttribute("googleCredential");
        Drive drive = GoogleAuthConfig.getDriveService(credential);

        File file = drive.files().get(fileId).setFields("mimeType,exportLinks").execute();

        if (file.getMimeType().startsWith("application/vnd.google-apps.")) {
            return drive.files().export(fileId, getExportMimeType(file.getMimeType())).executeMediaAsInputStream();
        }

        return drive.files().get(fileId).executeMediaAsInputStream();
    }

    public static String extractFileId(String url) {
        if (url.contains("/d/")) {
            int start = url.indexOf("/d/") + 3;
            int end = url.indexOf('/', start);
            if (end == -1) end = url.indexOf('?', start);
            return url.substring(start, end);
        }
        return null;
    }

    private static String getExportMimeType(String type) {
        return switch (type) {
            case "application/vnd.google-apps.document" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "application/vnd.google-apps.spreadsheet" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "application/vnd.google-apps.presentation" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/pdf";
        };
    }

    private static String getFolderId(String category, Drive drive) throws IOException {
        if (FOLDER_CACHE.containsKey(category)) return FOLDER_CACHE.get(category);

        String folderName = "ChurchDocs_" + category;
        FileList list = drive.files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false")
            .setFields("files(id)").execute();

        if (!list.getFiles().isEmpty()) {
            String id = list.getFiles().get(0).getId();
            FOLDER_CACHE.put(category, id);
            return id;
        }

        File folder = new File();
        folder.setName(folderName);
        folder.setMimeType("application/vnd.google-apps.folder");
        File created = drive.files().create(folder).setFields("id").execute();

        FOLDER_CACHE.put(category, created.getId());
        return created.getId();
    }

    public record UploadResult(String fileUrl, String fileId, String storageProvider) {}
}
