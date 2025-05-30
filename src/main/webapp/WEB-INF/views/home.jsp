<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="org.gihdm.model.Document" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Home - GraceDM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<header>
    <div class="menu-toggle" id="menuToggle">
        <span></span><span></span><span></span>
    </div>
    <div class="header-title">GraceDM</div>
</header>


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

<div class="main-content" id="mainContent">
    <div class="upload-container">
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
   <div id="contextMenu" class="context-menu">
       <div class="menu-item" id="downloadOption">Download</div>
       <div class="menu-divider"></div>
       <div class="menu-item" id="deleteOption">Delete</div>
   </div>
</body>
</html>