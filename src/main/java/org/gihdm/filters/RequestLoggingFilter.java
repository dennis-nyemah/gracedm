package org.gihdm.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@WebFilter("/*")
public class RequestLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Started {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed {} {} in {} ms", 
                request.getMethod(), 
                request.getRequestURI(), 
                duration);
        }
    }
}