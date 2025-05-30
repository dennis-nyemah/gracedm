package org.gihdm.services;

import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import org.gihdm.config.CloudinaryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    public static String upload(Part filePart) throws IOException {
        String fileName = filePart.getSubmittedFileName();
        logger.info("Starting Cloudinary upload for file: {}", fileName);

        File tempFile = File.createTempFile("upload_", fileName);
        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        Map<?, ?> uploadResult = CloudinaryConfig.getInstance().uploader().upload(
            tempFile,
            ObjectUtils.asMap("folder", "church_uploads", "resource_type", "auto")
        );

        String secureUrl = (String) uploadResult.get("secure_url");
        logger.info("Uploaded to Cloudinary: {}", secureUrl);

        Files.deleteIfExists(tempFile.toPath());
        return secureUrl;
    }

    public static void delete(String publicId) throws IOException {
        CloudinaryConfig.getInstance().uploader().destroy(publicId, ObjectUtils.emptyMap());
        logger.info("Deleted from Cloudinary: {}", publicId);
    }

    public static InputStream getFileStream(String publicId, String extension) throws IOException {
        String url = CloudinaryConfig.getInstance()
            .url()
            .resourceType("image")
            .format(extension)
            .generate(publicId);

        logger.debug("Cloudinary download URL: {}", url);
        return new URL(url).openStream();
    }

    public static String extractPublicId(String url) {
        int start = url.indexOf("church_uploads/");
        if (start != -1) {
            String after = url.substring(start);
            return after.substring(0, after.lastIndexOf('.'));
        }
        return null;
    }
}
