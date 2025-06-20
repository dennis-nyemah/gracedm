package org.gihdm.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.gihdm.config.GoogleAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.gihdm.model.UploadResult;

public class YouTubeService {
    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    private static final String APPLICATION_NAME = "GraceDM";
    private static final String CHANNEL_ID = "UCpytagMmy1p-F46FoJj_kkg";      
    private static final String DEFAULT_CATEGORY_ID = "22"; // People & Blogs category
    
    public static UploadResult upload(Part filePart, String category, HttpSession session) throws IOException {
        Credential credential = (Credential) session.getAttribute("googleCredential");
        if (credential == null) {
            throw new IOException("No Google credentials found in session");
        }

        YouTube youtube = new YouTube.Builder(
                GoogleAuthConfig.getHttpTransport(),
                GoogleAuthConfig.getJsonFactory(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        try {
            Video video = new Video();
            
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(filePart.getSubmittedFileName());
            snippet.setDescription("Uploaded from GraceDM - Category: " + category);
            snippet.setCategoryId(DEFAULT_CATEGORY_ID);
            snippet.setTags(Collections.singletonList("GraceDM"));
            video.setSnippet(snippet);
            
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("unlisted");
            video.setStatus(status);

            InputStreamContent mediaContent = new InputStreamContent(
                    filePart.getContentType(),
                    filePart.getInputStream());
            mediaContent.setLength(filePart.getSize());

            YouTube.Videos.Insert request = youtube.videos()
                    .insert("snippet,status", video, mediaContent);
            
            MediaHttpUploader uploader = request.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setProgressListener(new UploadProgressListener());
            
            Video uploadedVideo = request.execute();
            
            // Return embed URL instead of watch URL
            String embedUrl = "https://www.youtube.com/embed/" + uploadedVideo.getId();
            
            return new UploadResult(embedUrl, uploadedVideo.getId(), "YOUTUBE");
            
        } catch (GoogleJsonResponseException e) {
            logger.error("YouTube API error: {}", e.getDetails().getMessage(), e);
            throw new IOException("YouTube upload failed: " + e.getDetails().getMessage());
        } catch (IOException e) {
            logger.error("YouTube upload failed", e);
            throw new IOException("YouTube upload failed", e);
        }
    }

    public static void delete(String videoId, HttpSession session) throws IOException {
        Credential credential = (Credential) session.getAttribute("googleCredential");
        if (credential == null) {
            throw new IOException("No Google credentials found in session");
        }

        YouTube youtube = new YouTube.Builder(
                GoogleAuthConfig.getHttpTransport(),
                GoogleAuthConfig.getJsonFactory(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        try {
            youtube.videos().delete(videoId).execute();
            logger.info("Successfully deleted YouTube video: {}", videoId);
        } catch (GoogleJsonResponseException e) {
            logger.error("Failed to delete YouTube video: {}", e.getDetails().getMessage(), e);
            throw new IOException("Failed to delete YouTube video: " + e.getDetails().getMessage());
        }
    }

    public static String getVideoStream(String videoId) {
        // Return embed URL for the video player
        return "https://www.youtube.com/embed/" + videoId;
    }

    private static class UploadProgressListener implements MediaHttpUploaderProgressListener {
        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    logger.debug("Upload initiation started");
                    break;
                case INITIATION_COMPLETE:
                    logger.debug("Upload initiation complete");
                    break;
                case MEDIA_IN_PROGRESS:
                    logger.debug("Upload progress: {}%", uploader.getProgress() * 100);
                    break;
                case MEDIA_COMPLETE:
                    logger.debug("Upload complete");
                    break;
                case NOT_STARTED:
                    logger.debug("Upload not started");
                    break;
            }
        }
    }

}
