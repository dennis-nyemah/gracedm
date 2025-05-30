package org.gihdm.security;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSRFTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(CSRFTokenManager.class);
    private static final SecureRandom random = new SecureRandom();
    private static final String CSRF_TOKEN_ATTR = "csrfToken";

    public static String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        logger.debug("Generated new CSRF token: [redacted]");
        return token;
    }

    public static void storeToken(HttpSession session) {
        if (session == null) {
            logger.warn("Attempt to store CSRF token in null session");
            return;
        }
        String token = generateToken();
        session.setAttribute(CSRF_TOKEN_ATTR, token);
        logger.debug("Stored CSRF token in session: {}", session.getId());
    }

    public static boolean isValidToken(HttpSession session, String requestToken) {
        if (session == null || requestToken == null) {
            logger.warn("CSRF validation failed - null session or token");
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        boolean isValid = sessionToken != null && sessionToken.equals(requestToken);
        
        if (!isValid) {
            logger.warn("CSRF token validation failed for session: {}. Possible CSRF attack!", session.getId());
        } else {
            logger.debug("CSRF token validated successfully for session: {}", session.getId());
        }
        
        return isValid;
    }
}