package org.gihdm.model;

public class UploadResult {
    private final String fileUrl;
    private final String fileId;
    private final String storageProvider;

    public UploadResult(String fileUrl, String fileId, String storageProvider) {
        this.fileUrl = fileUrl;
        this.fileId = fileId;
        this.storageProvider = storageProvider;
    }

    public String fileUrl() {
        return fileUrl;
    }

    public String fileId() {
        return fileId;
    }

    public String storageProvider() {
        return storageProvider;
    }
}