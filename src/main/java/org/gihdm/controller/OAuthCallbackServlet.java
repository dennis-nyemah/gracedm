package org.gihdm.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.gihdm.config.GoogleAuthConfig;
import org.gihdm.security.CSRFTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

@WebServlet("/oauth2callback")
public class OAuthCallbackServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OAuthCallbackServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        if (code == null) {
            logger.warn("Missing authorization code");
            resp.sendRedirect("index.jsp?error=auth_failed");
            return;
        }

   
        try {
            HttpSession session = req.getSession();
            GoogleAuthorizationCodeFlow flow = GoogleAuthConfig.getFlow();

            // Exchange code for tokens
            TokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(getRedirectUri(req))
                .execute();
            logger.info("Granted scopes from token response: {}", tokenResponse.getScope());


            // Verify ID token
            String idTokenString = (String) tokenResponse.get("id_token");
            if (idTokenString == null) {
                throw new RuntimeException("No ID token in response");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
            ).setAudience(Collections.singletonList(GoogleAuthConfig.getClientId()))
             .build();

            GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), idTokenString);
            if (idToken == null) throw new RuntimeException("Invalid ID token");

            // Extract email
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            if (email == null) email = "google-" + payload.getSubject();

            // Store credentials
            Credential credential = flow.createAndStoreCredential(tokenResponse, email);

            // Store session data
            session.setAttribute("googleCredential", credential);
            session.setAttribute("googleUserEmail", email);
            session.setAttribute("googleToken", credential.getAccessToken()); // Optional
            CSRFTokenManager.storeToken(session);

            logger.info("Authenticated user: {}", email);
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage(), e);
            resp.sendRedirect("index.jsp?error=auth_error");
        }
    }
    
    
    private String getRedirectUri(HttpServletRequest req) {
        return "https" + "://" + req.getServerName() +
            (req.getServerPort() == 80 ? "" : ":" + req.getServerPort()) +
            req.getContextPath() + "/oauth2callback";
    }
}