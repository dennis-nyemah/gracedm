package org.gihdm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTypeUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileTypeUtil.class);

    public static String getFileType(String filename) {
        if (filename == null) {
            logger.debug("Null filename provided, returning 'unknown' type");
            return "unknown";
        }
        
        if (!filename.contains(".")) {
            logger.debug("Filename '{}' has no extension, returning 'unknown' type", 
                truncateFilename(filename));
            return "unknown";
        }

        String fileType = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        logger.trace("Detected file type '{}' for filename '{}'", 
            fileType, truncateFilename(filename));
        return fileType;
    }

    public static boolean isImage(String fileType) {
        boolean isImage = fileType.matches("jpg|jpeg|png|gif|webp|m4a");
        logger.trace("Checking if '{}' is image: {}", fileType, isImage);
        return isImage;
    }

    public static boolean isVideo(String fileType) {
        boolean isVideo = fileType.matches("mp4|mov|avi|mkv|webm");
        logger.trace("Checking if '{}' is video: {}", fileType, isVideo);
        return isVideo;
    }

    private static String truncateFilename(String filename) {
        if (filename == null) return "null";
        return filename.length() > 50 
            ? filename.substring(0, 47) + "..." 
            : filename;
    }
}