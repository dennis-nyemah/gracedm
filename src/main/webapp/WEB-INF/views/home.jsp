<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<%@ page import="org.gihdm.model.Document" %>

<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors"%>
<%@ page import="java.util.List" %>

<%@ page import="java.net.URLEncoder" %>

<!DOCTYPE html>
<html>
<head> 
    <meta charset="UTF-8">
    <title>Home - GraceDM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="logo">GraceDM</div>
        <div class="menu-toggle" id="menuToggle">
            <i class="fas fa-bars"></i>
        </div>
    </header>

    <!-- Sidebar (maintained from original) -->
    <div class="sidebar" id="sidebar">
        <div class="sidebar-header">
            <h3>Menu</h3>      
            <span class="close-btn" id="closeBtn">&times;</span>
        </div>

        <div class="sidebar-content" style="overflow-y: auto; max-height: 85vh;">
            <%
            Map<String, List<Document>> documentsByCategory =
                (Map<String, List<Document>>) request.getAttribute("documentsByCategory");
            String currentCategory = (String) request.getAttribute("currentCategory");

            String[][] categories = {
                {"Documents", "Letters", "Certificates", "Program Sheets"},
                {"Videos", "Sermons", "Service Moments", "Events"},
                {"Pictures", "Services", "Programs", "Others"}
            };
            %>

            <% for (String[] group : categories) { %>
            <div class="sidebar-item" data-category-type="<%= group[0].toLowerCase() %>">
                <div class="sidebar-item-title active">
                    <span class="group-title-text"><%= group[0] %></span>
                </div>
                <div class="sidebar-submenu active">
                    <% for (int i = 1; i < group.length; i++) {
                        String category = group[i];
                        String categoryKey = category; 
                        List<Document> docs = documentsByCategory != null ? documentsByCategory.get(categoryKey) : null;
                    %>
                    <div class="sidebar-subitem">
                        <strong>
                            <a href="?category=<%= URLEncoder.encode(category, "UTF-8") %>">
                                <%= category.replace("_", " ") %>
                            </a>
                        </strong>
                        <% if (docs != null && !docs.isEmpty()) { %>
                            <ul class="file-list" style="margin-left: 15px; font-size: 0.9rem;">
                              <% for (Document doc : docs) { %>
                                <li data-category-type="<%= group[0].toLowerCase() %>" 
                                    data-file-id="<%= doc.getId() %>"
                                    data-file-url="<%= doc.getCloudUrl() %>">
                                    <a href="#" class="file-link">
                                        <%= doc.getTitle() %>
                                    </a>
                                  </li>
                              <% } %>
                            </ul>
                        <% } else { %>
                            <div style="margin-left: 15px; color: #888;">No documents</div>
                        <% } %>
                    </div>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>
    </div>

    <div class="overlay" id="overlay"></div>
   
    <!-- Main Content -->
    <div class="main-content" id="mainContent">
        <!-- Default view shows upload interface -->
        <div class="upload-container" id="uploadView">
            <div class="upload-box">
                <div class="upload-icon">📁</div>
                <div id="upload-section">
                   <h2 id="upload-title">
                      <%= currentCategory != null ? "Upload to " + currentCategory.replace("_", " ") : "Upload a Document" %>
                   </h2>
                   <p class="upload-instructions">Drag & drop files here or click the button below</p>
                   <form id="uploadForm" action="upload" method="post" enctype="multipart/form-data">
                       <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                       <input type="hidden" id="selectedCategory" name="category" value="<%= currentCategory != null ? currentCategory : "Letters" %>">
                       <button type="button" class="upload-btn" id="uploadBtn">Choose Files</button>
                       <input type="file" id="fileInput" name="file" multiple>
                       <div class="file-info" id="fileInfo">No files selected</div>
                       <button type="submit" class="upload-btn" id="submitBtn" style="display:none; margin-top:10px;">Upload Selected Files</button>
                   </form>
                </div>
            </div>
        </div>
        
        <!-- Media feed views --> 
 <!-- Videos Feed -->
<div class="content-view" id="videosView" style="display:none;">
    <h2 class="content-title">Videos</h2>
    <div class="media-feed">
        <% 
        List<Document> videos = (List<Document>) request.getAttribute("allVideos");
        if (videos != null) {
            for (Document video : videos) { 
                boolean isYouTube = "YOUTUBE".equals(video.getStorageProvider());
        %>
        <div class="media-card">
            <% if (isYouTube) { %>
                <div class="video-container">
                    <iframe src="https://www.youtube.com/embed/<%= video.getFileId() %>" 
                            frameborder="0" 
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                            allowfullscreen
                            loading="lazy">
                    </iframe>
                </div>
            <% } else { %>
                <div class="media-thumbnail video" data-file-url="<%= video.getCloudUrl() %>">
                    <i class="fas fa-play"></i>
                </div>
            <% } %>
            <div class="media-info">
                <div class="media-title"><%= video.getTitle() %></div>
                <div class="media-meta">
                    <%= video.getUploadedAt() %> • 
                    <span class="media-category"><%= video.getCategory() %></span>
                </div>
            </div>
        </div>
        <%   } 
            if (videos.isEmpty()) { %>
        <div class="empty-state">
            <i class="fas fa-video-slash"></i>
            <p>No videos available</p>
        </div>
        <%   }
        } else { %>
        <div class="empty-state">
            <i class="fas fa-video-slash"></i>
            <p>No videos available</p>
        </div>
        <% } %>
    </div>
</div>

<!-- Pictures Feed -->
<div class="content-view" id="picturesView" style="display:none;">
    <h2 class="content-title">Pictures</h2>
    <div class="media-feed">
        <% 
        List<Document> pictures = (List<Document>) request.getAttribute("allPictures");
        if (pictures != null) { 
            for (Document picture : pictures) { 
                boolean isCloudinary = "CLOUDINARY".equals(picture.getStorageProvider());
        %>
        <div class="media-card">
            <% if (isCloudinary) { %>
                <div class="image-container">
                    <img src="<%= picture.getCloudUrl() %>" 
                         alt="<%= picture.getTitle() %>"
                         loading="lazy"
                         class="cloudinary-image">
                </div>
            <% } else { %>
                <div class="media-thumbnail image" data-file-url="<%= picture.getCloudUrl() %>">
                    <i class="fas fa-image"></i>
                </div>
            <% } %>
            <div class="media-info">
                <div class="media-title"><%= picture.getTitle() %></div>
                <div class="media-meta">
                    <%= picture.getUploadedAt() %> • 
                    <span class="media-category"><%= picture.getCategory() %></span>
                </div>
            </div>
        </div>
        <%   } 
            if (pictures.isEmpty()) { %>
        <div class="empty-state">
            <i class="fas fa-image"></i>
            <p>No pictures available</p>
        </div>
        <%   }
        } else { %>
        <div class="empty-state">
            <i class="fas fa-image"></i>
            <p>No pictures available</p>
        </div>
        <% } %>
    </div>
</div>

<!-- Documents Feed -->
<div class="content-view" id="documentsView" style="display:none;">
    <h2 class="content-title">Documents</h2>
    <div class="media-feed">
        <% 
        List<Document> documents = (List<Document>) request.getAttribute("allDocuments"); 
        if (documents != null) {
            for (Document doc : documents) { 
        %>
        <div class="media-card">
            <div class="media-thumbnail document" data-file-url="<%= doc.getCloudUrl() %>">
                <i class="fas fa-file-alt"></i>
            </div>
            <div class="media-info">
                <div class="media-title"><%= doc.getTitle() %></div>
                <div class="media-meta">
                    <%= doc.getUploadedAt() %> • 
                    <span class="media-category"><%= doc.getCategory() %></span>
                </div>
            </div>
        </div>
        <%   } 
            if (documents.isEmpty()) { %>
        <div class="empty-state">
            <i class="fas fa-file-alt"></i>
            <p>No documents available</p>
        </div>
        <%   }
        } else { %>
        <div class="empty-state">
            <i class="fas fa-file-alt"></i>
            <p>No documents available</p>
        </div>
        <% } %>
    </div>
</div>

    <!-- Bottom Navigation -->
    <div class="bottom-nav">
        <div class="nav-item active" data-view="upload">
            <div class="nav-icon"><i class="fas fa-cloud-upload-alt"></i></div>
            <div class="nav-label">Upload</div>
        </div>
        <div class="nav-item" data-view="videos">
            <div class="nav-icon"><i class="fas fa-play-circle"></i></div>
            <div class="nav-label">Videos</div>
        </div>
        <div class="nav-item" data-view="pictures">
            <div class="nav-icon"><i class="fas fa-images"></i></div>
            <div class="nav-label">Pictures</div>
        </div>
        <div class="nav-item" data-view="documents">
            <div class="nav-icon"><i class="fas fa-file-alt"></i></div>
            <div class="nav-label">Documents</div>
        </div>
    </div>

    <!-- Context Menu -->
    <div id="contextMenu" class="context-menu">
        <div class="menu-item" id="downloadOption">Download</div>
        <div class="menu-divider"></div>
        <div class="menu-item" id="deleteOption">Delete</div>
    </div>

    <script src="${pageContext.request.contextPath}/js/script.js"></script>
    <% if (request.getAttribute("toastMessage") != null) { %>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            showToast('<%= request.getAttribute("toastMessage") %>',
                      '<%= request.getAttribute("toastType") != null ? request.getAttribute("toastType") : "success" %>');
        });
    </script>
    <% } %>
    
</body>
</html> 