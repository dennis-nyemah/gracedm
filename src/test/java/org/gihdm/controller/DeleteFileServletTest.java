/*
package org.gihdm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.gihdm.model.Document;
import org.gihdm.repository.DocumentRepository;
import org.gihdm.services.CloudinaryService;
import org.gihdm.services.GoogleDriveService;
import org.gihdm.services.YouTubeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DocumentRepository.class, CloudinaryService.class, GoogleDriveService.class, YouTubeService.class})
public class DeleteFileServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private DeleteFileServlet servlet;

    private final String testUserEmail = "test@example.com";
    private final String testToken = "test-token";
    private final String testFileId = "test-file-id";

    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        servlet = new DeleteFileServlet();
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("googleToken")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(401, "Unauthorized");
    }

    @Test
    public void testDocumentNotFound() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("googleToken")).thenReturn(testToken);
        when(session.getAttribute("googleUser")).thenReturn(testUserEmail);
        when(request.getParameter("id")).thenReturn(testFileId);

        DocumentRepository mockRepo = mock(DocumentRepository.class);
        when(mockRepo.findById(testFileId)).thenReturn(null);
        PowerMockito.whenNew(DocumentRepository.class).withNoArguments().thenReturn(mockRepo);

        servlet.doPost(request, response);

        verify(response).sendError(403, "Forbidden");
    }

    @Test
    public void testForbiddenNotOwner() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("googleToken")).thenReturn(testToken);
        when(session.getAttribute("googleUser")).thenReturn("other@user.com");
        when(request.getParameter("id")).thenReturn(testFileId);

        Document doc = createTestDocument(testUserEmail, "DRIVE");
        DocumentRepository mockRepo = mock(DocumentRepository.class);
        when(mockRepo.findById(testFileId)).thenReturn(doc);
        PowerMockito.whenNew(DocumentRepository.class).withNoArguments().thenReturn(mockRepo);

        servlet.doPost(request, response);

        verify(response).sendError(403, "Forbidden");
    }

    @Test
    public void testSuccessfulCloudinaryDelete() throws Exception {
        testSuccessfulDeletion("CLOUDINARY");
    }

    @Test
    public void testSuccessfulDriveDelete() throws Exception {
        testSuccessfulDeletion("DRIVE");
    }

    @Test
    public void testSuccessfulYouTubeDelete() throws Exception {
        testSuccessfulDeletion("YOUTUBE");
    }

    private void testSuccessfulDeletion(String provider) throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("googleToken")).thenReturn(testToken);
        when(session.getAttribute("googleUser")).thenReturn(testUserEmail);
        when(request.getParameter("id")).thenReturn(testFileId);

        Document doc = createTestDocument(testUserEmail, provider);
        DocumentRepository mockRepo = mock(DocumentRepository.class);
        when(mockRepo.findById(testFileId)).thenReturn(doc);
        PowerMockito.whenNew(DocumentRepository.class).withNoArguments().thenReturn(mockRepo);

        if ("DRIVE".equals(provider)) {
            GoogleDriveService driveService = mock(GoogleDriveService.class);
            PowerMockito.whenNew(GoogleDriveService.class).withNoArguments().thenReturn(driveService);
        } else if ("YOUTUBE".equals(provider)) {
            YouTubeService ytService = mock(YouTubeService.class);
            PowerMockito.whenNew(YouTubeService.class).withNoArguments().thenReturn(ytService);
        } else {
            CloudinaryService cloudService = mock(CloudinaryService.class);
            PowerMockito.whenNew(CloudinaryService.class).withNoArguments().thenReturn(cloudService);
        }

        servlet.doPost(request, response);

        verify(mockRepo).delete(testFileId);
        verify(response).setStatus(200);
    }

    @Test
    public void testUnknownStorageProvider() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("googleToken")).thenReturn(testToken);
        when(session.getAttribute("googleUser")).thenReturn(testUserEmail);
        when(request.getParameter("id")).thenReturn(testFileId);

        Document doc = createTestDocument(testUserEmail, "UNKNOWN");
        DocumentRepository mockRepo = mock(DocumentRepository.class);
        when(mockRepo.findById(testFileId)).thenReturn(doc);
        PowerMockito.whenNew(DocumentRepository.class).withNoArguments().thenReturn(mockRepo);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        verify(response).setStatus(500);
        pw.flush();  // Ensure the writer flushes its content
        assertTrue(sw.toString().contains("Deletion failed"));
    }

    private Document createTestDocument(String uploadedBy, String storageProvider) {
        Document doc = new Document(
                "test.pdf",
                "http://example.com/test.pdf",
                "test-category",
                storageProvider,
                uploadedBy
        );
        doc.setId(testFileId);
        doc.setFileId(testFileId);
        return doc;
    }
}
*/