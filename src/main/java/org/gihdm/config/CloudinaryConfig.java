package org.gihdm.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CloudinaryConfig {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);
    private static final Cloudinary cloudinary;

    static {
        logger.info("Initializing Cloudinary configuration");
        
        // Try environment variables first (Render production)
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");

        if (cloudName != null && apiKey != null && apiSecret != null) {
            logger.debug("Using Cloudinary config from environment variables");
        } else {
            logger.debug("Falling back to properties file configuration");
            try {
                Properties props = new Properties();
                // Load application.properties
                try (InputStream input = CloudinaryConfig.class
                        .getResourceAsStream("/application.properties")) {
                    if (input == null) {
                        throw new IOException("application.properties not found");
                    }
                    props.load(input);
                }

                // Get the path to cloudinary properties
                String cloudinaryConfigPath = props.getProperty("cloudinary.config.path");
                if (cloudinaryConfigPath == null) {
                    throw new IOException("cloudinary.config.path not specified");
                }
                cloudinaryConfigPath = cloudinaryConfigPath.replace("classpath:", "/");
                logger.debug("Resolved Cloudinary config path: {}", cloudinaryConfigPath);

                // Load the actual cloudinary properties
                try (InputStream cloudinaryInput = CloudinaryConfig.class
                        .getResourceAsStream(cloudinaryConfigPath)) {
                    if (cloudinaryInput == null) {
                        throw new IOException("Cloudinary config file not found at " + cloudinaryConfigPath);
                    }
                    props.load(cloudinaryInput);
                }

                cloudName = props.getProperty("cloudinary.cloud_name");
                apiKey = props.getProperty("cloudinary.api_key");
                apiSecret = props.getProperty("cloudinary.api_secret");

                if (cloudName == null || apiKey == null || apiSecret == null) {
                    throw new IOException("Missing required Cloudinary properties");
                }
                
                logger.debug("Successfully loaded Cloudinary config from properties file");
            } catch (IOException e) {
                logger.error("Failed to load Cloudinary configuration", e);
                throw new RuntimeException("Failed to load Cloudinary configuration", e);
            }
        }

        // Initialize Cloudinary
        logger.info("Initializing Cloudinary client with cloud name: {}", cloudName);
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public static Cloudinary getInstance() {
        logger.debug("Providing Cloudinary instance");
        return cloudinary;
    }
}