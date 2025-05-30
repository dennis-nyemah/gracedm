package org.gihdm.controller;

import com.google.api.client.auth.oauth2.Credential;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/debug")
public class AuthDebugServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession(false);
        
        if (session == null) {
            out.println("No active session");
            return;
        }

        Credential cred = (Credential) session.getAttribute("googleCredential");
        out.println("User Email: " + session.getAttribute("googleUserEmail"));
        out.println("Credential: " + (cred != null ? "Valid" : "Null"));
        
        if (cred != null) {
            out.println("\nCredential Details:");
            out.println("Access Token: " + (cred.getAccessToken() != null ? "Exists" : "Null"));
            out.println("Refresh Token: " + (cred.getRefreshToken() != null ? "Exists" : "Null"));
            out.println("Expires In: " + cred.getExpiresInSeconds() + " seconds");
        }
    }
}