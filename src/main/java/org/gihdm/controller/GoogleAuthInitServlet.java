package org.gihdm.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gihdm.config.GoogleAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/auth/google")
public class GoogleAuthInitServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthInitServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            logger.debug("Initializing Google OAuth flow...");

            GoogleAuthorizationCodeFlow flow = GoogleAuthConfig.getFlow();

            String redirectUri = "https" + "://" + req.getServerName() +
                (req.getServerPort() == 80 ? "" : ":" + req.getServerPort()) +
                req.getContextPath() + "/oauth2callback";

            logger.debug("Using redirect URI: {}", redirectUri);

            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setScopes(GoogleAuthConfig.SCOPES)
                    .build();

            logger.debug("Final authorization URL: {}", authorizationUrl.replace(flow.getClientId(), "[CLIENT_ID_REDACTED]"));
            resp.sendRedirect(authorizationUrl);

        } catch (Exception e) {
            logger.error("Failed to initialize Google auth", e);
            resp.sendError(500, "Failed to initialize Google auth");
        }
    }
}
