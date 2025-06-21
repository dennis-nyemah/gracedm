package org.gihdm.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CSPFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResp = (HttpServletResponse) response;

        httpResp.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://www.youtube.com https://s.ytimg.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://use.fontawesome.com https://www.youtube.com; " +
            "style-src-elem 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://use.fontawesome.com https://www.youtube.com; " +
            "img-src 'self' data: https://res.cloudinary.com https://drive.google.com https://www.youtube.com https://i.ytimg.com https://developers.google.com; " +
            "font-src 'self' https://fonts.gstatic.com https://use.fontawesome.com https://cdnjs.cloudflare.com; " +
            "media-src 'self' https://drive.google.com https://www.youtube.com; " +
            "connect-src 'self' https://www.youtube.com; " +
            "object-src 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'; " +
            "frame-src https://drive.google.com https://accounts.google.com https://docs.google.com https://www.youtube.com https://youtube.com; " +
            "child-src https://www.youtube.com; " +
            "frame-ancestors 'self';"
        );

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}