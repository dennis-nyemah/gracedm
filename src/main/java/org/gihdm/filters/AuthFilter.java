package org.gihdm.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing AuthFilter");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length()).toLowerCase();
        String method = request.getMethod();

        logger.trace("Filtering request: {} {}", method, path);

        // Allow public resources
        if (isPublicResource(path)) {
            logger.debug("Allowing public resource: {}", path);
            addCookieAttributes(response);
            chain.doFilter(request, response);
            return;
        }

        // Session validation
        HttpSession session = request.getSession(false);
        String userEmail = session != null ? (String) session.getAttribute("googleUserEmail") : null;

        if (session == null || userEmail == null) {
            logger.warn("Unauthorized access attempt to {} by user: {}", 
                path, userEmail != null ? userEmail : "no-session");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        // Apply security headers
        applySecurityHeaders(response);
        
        // Add cookie attributes
        addCookieAttributes(response);
        
        logger.debug("Authorized request to {} by user: {}", path, userEmail);
        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        return path.equals("/index.jsp") || 
        	   path.equals("/healthz") ||
               path.equals("/oauth2callback") ||
               path.equals("/auth/google") ||
               path.startsWith("/css/") ||  
               path.startsWith("/js/") ||
        	   path.startsWith("/images/") ||
        	   path.endsWith(".ico") ||
        	   path.endsWith(".png") ||
        	   path.endsWith(".jpg") ||
        	   path.endsWith(".woff") ||
        	   path.endsWith(".woff2") ||
        	   path.endsWith(".ttf");
    }
    
    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Feature-Policy", 
            "geolocation 'none'; microphone 'none'; camera 'none'");
    }

    private void addCookieAttributes(HttpServletResponse response) {
        Collection<String> headers = response.getHeaders("Set-Cookie");
        if (headers == null || headers.isEmpty()) {
            return;
        }

        List<String> updatedHeaders = new ArrayList<>();
        for (String header : headers) {
            StringBuilder newHeader = new StringBuilder(header);
            
            if (!header.contains("SameSite")) {
                newHeader.append("; SameSite=Lax");
            }
            if (!header.contains("Secure") ) {
                newHeader.append("; Secure");
            }
            if (!header.contains("HttpOnly")) {
                newHeader.append("; HttpOnly");
            }
            if (!header.contains("Path=")) {
                newHeader.append("; Path=/");
            }
            
            updatedHeaders.add(newHeader.toString());
        }
        
        if (!updatedHeaders.isEmpty()) {
            response.setHeader("Set-Cookie", String.join(", ", updatedHeaders));
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying AuthFilter");
    }
}