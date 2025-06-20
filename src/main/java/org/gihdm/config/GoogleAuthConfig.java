package org.gihdm.config;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.security.GeneralSecurityException;

public class GoogleAuthConfig {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthConfig.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final List<String> SCOPES = List.of(
        DriveScopes.DRIVE,
        "https://www.googleapis.com/auth/youtube.upload",
        "https://www.googleapis.com/auth/youtube",
        "openid",
      	"email" 		
    );   
    
    private static GoogleClientSecrets clientSecrets;
    private static Credential credential;
    private static GoogleAuthorizationCodeFlow flow;

    public static String getClientId() {
        if (clientSecrets == null) {
            throw new IllegalStateException("GoogleClientSecrets not initialized");
        }
        return clientSecrets.getDetails().getClientId();
    }

    public static synchronized GoogleAuthorizationCodeFlow getFlow() throws IOException {
        if (flow == null) {
            logger.info("Initializing Google Authorization Flow");
            
            // Try environment variables first
            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
            
            if (clientId != null && clientSecret != null) {
                logger.debug("Using Google credentials from environment variables");
                clientSecrets = new GoogleClientSecrets()
                    .setWeb(new GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRedirectUris(List.of(System.getenv("GOOGLE_REDIRECT_URI"))));
            } else {
                // Fallback to JSON credentials file
                String jsonPath = System.getenv("GOOGLE_CREDENTIALS_PATH");
                if (jsonPath == null) {
                    Properties props = new Properties();
                    try (InputStream input = GoogleAuthConfig.class.getResourceAsStream("/application.properties")) {
                        if (input == null) throw new IOException("application.properties not found");
                        props.load(input);
                        jsonPath = props.getProperty("google.credentials.path");
                        if (jsonPath == null) throw new IOException("google.credentials.path not specified");
                    }
                }

                logger.debug("Loading client secrets from: {}", jsonPath);
                try (InputStreamReader reader = new InputStreamReader(
                        GoogleAuthConfig.class.getResourceAsStream(jsonPath.replace("classpath:", "/"))))
                {
                    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
                }
            }

            flow = new GoogleAuthorizationCodeFlow.Builder(
                    getHttpTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .build();

            logger.info("Authorization flow initialized for client ID: {}", clientSecrets.getDetails().getClientId());
        }
        return flow;
    }
 
    public static NetHttpTransport getHttpTransport() {
        try {
            logger.debug("Creating new trusted HTTP transport");
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to initialize HTTP transport", e);
            throw new RuntimeException("Failed to initialize HTTP transport", e);
        }
    }

    public static JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }

    public static Credential getCredential(String accessToken) {
        logger.debug("Getting credential for access token");
        if (credential == null || !credential.getAccessToken().equals(accessToken)) {
            logger.debug("Creating new credential instance");
            credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(getHttpTransport())
                    .build()
                    .setAccessToken(accessToken);
        }
        return credential;
    }

    public static Drive getDriveService(Credential credential) {
        logger.debug("Creating Drive service instance");
        return new Drive.Builder(
                getHttpTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName("GraceDM").build();
    }
}