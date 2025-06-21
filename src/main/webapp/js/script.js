// DOM Elements with null checks
const overlay = document.getElementById('overlay');
const menuToggle = document.getElementById('menuToggle');
const closeBtn = document.getElementById('closeBtn');
const sidebar = document.getElementById('sidebar');
const uploadBtn = document.getElementById('uploadBtn');
const submitBtn = document.getElementById('submitBtn');
const fileInput = document.getElementById('fileInput');
const fileInfo = document.getElementById('fileInfo');
const selectedCategory = document.getElementById('selectedCategory');
const uploadForm = document.getElementById('uploadForm');
const uploadTitle = document.getElementById('upload-title');
const uploadView = document.getElementById('uploadView');
const videosView = document.getElementById('videosView');
const picturesView = document.getElementById('picturesView');
const documentsView = document.getElementById('documentsView');
const contextMenu = document.getElementById('contextMenu');

// Sidebar toggling with null checks
if (menuToggle && sidebar && overlay) {
    menuToggle.addEventListener('click', function() {
        sidebar.style.width = '300px';
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    });
}

if (closeBtn && sidebar && overlay) {
    closeBtn.addEventListener('click', closeSidebar);
}

if (overlay) {
    overlay.addEventListener('click', closeSidebar);
}

function closeSidebar() {
    if (sidebar) sidebar.style.width = '0';
    if (overlay) overlay.classList.remove('active');
    document.body.style.overflow = 'auto';
}

// Dropdown toggle functionality
const sidebarTitles = document.querySelectorAll('.sidebar-item-title');
sidebarTitles.forEach(title => {
    title.addEventListener('click', function(e) {
        e.stopPropagation();
        this.classList.toggle('active');
        const submenu = this.nextElementSibling;
        if (submenu) submenu.classList.toggle('active');
    });
});

// Navigation switching with null checks
const navItems = document.querySelectorAll('.nav-item');
if (navItems.length > 0) {
    navItems.forEach(item => {
        item.addEventListener('click', function() {
            // Update active state
            navItems.forEach(i => i.classList.remove('active'));
            this.classList.add('active');
            
            // Get the view to show
            const view = this.getAttribute('data-view');
            
            // Hide all views
            if (uploadView) uploadView.style.display = 'none';
            if (videosView) videosView.style.display = 'none';
            if (picturesView) picturesView.style.display = 'none';
            if (documentsView) documentsView.style.display = 'none';
            
            // Show selected view
            switch(view) {
                case 'upload':
                    if (uploadView) uploadView.style.display = 'flex';
                    break;
                case 'videos':
                    if (videosView) videosView.style.display = 'block';
                    break;
                case 'pictures':
                    if (picturesView) picturesView.style.display = 'block';
                    break;
                case 'documents':
                    if (documentsView) documentsView.style.display = 'block';
                    break;
            }
        });
    });
} else {
    console.warn("No navigation items found");
}

// Media thumbnail and image click handlers
const mediaThumbnails = document.querySelectorAll('.media-thumbnail');
const cloudinaryImages = document.querySelectorAll('.cloudinary-image');

if (mediaThumbnails.length > 0) {
    mediaThumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const fileUrl = this.getAttribute('data-file-url');
            if (fileUrl) {
                window.open(fileUrl, '_blank');
            }
        });
    });
}

// Image modal functionality for Cloudinary images
if (cloudinaryImages.length > 0) {
    cloudinaryImages.forEach(img => {
        img.addEventListener('click', function(e) {
            e.preventDefault();
            const modal = document.createElement('div');
            modal.className = 'modal';
            modal.innerHTML = `
                <span class="close-modal">&times;</span>
                <img class="modal-content" src="${this.src}" alt="${this.alt}">
            `;
            document.body.appendChild(modal);
            
            // Show the modal
            modal.style.display = 'block';
            
            // Close when clicking X or outside image
            modal.querySelector('.close-modal').addEventListener('click', () => {
                modal.style.display = 'none';
                setTimeout(() => modal.remove(), 300);
            });
            
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.style.display = 'none';
                    setTimeout(() => modal.remove(), 300);
                }
            });
        });
    });
}

// File upload handling with null checks
if (uploadBtn && fileInput) {
    uploadBtn.addEventListener('click', function() {
        fileInput.click();
    });

    fileInput.addEventListener('change', function() {
        if (fileInput.files.length > 0 && fileInfo && selectedCategory) {
            const names = [];
            for (let i = 0; i < fileInput.files.length; i++) {
                names.push(fileInput.files[i].name);
            }
            const currentCat = selectedCategory.value.replace(/_/g, ' ');
            fileInfo.textContent = 'Uploading ' + fileInput.files.length + ' file(s) to ' + currentCat + ': ' + names.join(', ');
            fileInfo.style.color = '#2196F3';
            if (submitBtn) submitBtn.style.display = 'block';
        } else if (fileInfo) {
            fileInfo.textContent = 'No files selected';
            fileInfo.style.color = '#666';
            if (submitBtn) submitBtn.style.display = 'none';
        }
    });
}

// Drag & drop support
const uploadBox = document.querySelector('.upload-box');
if (uploadBox && fileInput) {
    uploadBox.addEventListener('dragover', function(e) {
        e.preventDefault();
        uploadBox.style.border = '2px dashed #2196F3';
    });

    uploadBox.addEventListener('dragleave', function() {
        uploadBox.style.border = 'none';
    });

    uploadBox.addEventListener('drop', function(e) {
        e.preventDefault();
        uploadBox.style.border = 'none';
        if (e.dataTransfer.files.length) {
            fileInput.files = e.dataTransfer.files;
            fileInput.dispatchEvent(new Event('change'));
        }
    });
}

function refreshMediaFeeds() {
    // This would ideally be an AJAX call to reload the feeds
    // For now, I just reload the page
    window.location.reload();
}

// this will be called after successful uploads/deletions
function handleFileChange() {
    setTimeout(refreshMediaFeeds, 2000);
}

// Form submission
if (uploadForm && selectedCategory) {
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        const xhr = new XMLHttpRequest();
        
        // Show loading state
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
        }

        // Progress feedback
        xhr.upload.addEventListener('progress', function(e) {
            if (e.lengthComputable && fileInfo) {
                const percent = Math.round((e.loaded / e.total) * 100);
                fileInfo.textContent = `Uploading... ${percent}%`;
            }
        });

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    showToast('Upload completed successfully', 'success');
                    handleFileChange();
                } else {
                    showToast('Upload failed. Please try again.', 'error');
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = 'Upload Selected Files';
                    }
                    if (fileInfo) {
                        fileInfo.textContent = 'Upload failed. Please try again.';
                        fileInfo.style.color = '#f44336';
                    }
                }
            }
        };

        const urlParams = new URLSearchParams(window.location.search);
        const category = urlParams.get('category') ||
                        sessionStorage.getItem('lastCategory') ||
                        selectedCategory.value ||
                        'Letters';

        selectedCategory.value = category;
        xhr.open('POST', this.action, true);
        xhr.send(formData);
    });
}

// Initialize context menu if it exists
if (contextMenu) {
    setupContextMenu();
}

// Set initial category title
if (uploadTitle && selectedCategory) {
    const urlParams = new URLSearchParams(window.location.search);
    const currentCat = urlParams.get('category') || selectedCategory.value || 'Letters';
    uploadTitle.textContent = 'Upload to ' + currentCat.replace(/_/g, ' ');
}

// Toast messages
function showToast(message, type) {
    type = type || 'success';
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type + ' show';
    toast.innerHTML = '<span class="toast-icon">' + (type === 'success' ? '✓' : '⚠') + '</span>' +
                      '<span class="toast-message">' + message + '</span>';
    document.body.appendChild(toast);
    setTimeout(function() {
        toast.classList.add('fade-out');
        setTimeout(function() {
            toast.remove();
        }, 300);
    }, 3000);
}

// Context Menu Implementation
function setupContextMenu() {
    console.log("Setting up context menu...");
    let currentFileId = null;
    let currentFileUrl = null;

    const fileLinks = document.querySelectorAll('.file-link');
    if (fileLinks.length > 0) {
        fileLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const li = this.closest('li');
                if (li && li.dataset.fileUrl) {
                    window.open(li.dataset.fileUrl, '_blank');
                }
            });
        });
    }

    const fileItems = document.querySelectorAll('li[data-file-id]');
    if (fileItems.length > 0) {
        fileItems.forEach(item => {
            // Right-click context menu
            item.addEventListener('contextmenu', function(e) {
                e.preventDefault();
                showContextMenu(e, this);
            });

            // Long-press for touch devices
            let pressTimer;
            item.addEventListener('touchstart', function(e) {
                pressTimer = setTimeout(function() {
                    e.preventDefault();
                    showContextMenu(e, this);
                }.bind(this), 500);
            });

            item.addEventListener('touchend', function() {
                clearTimeout(pressTimer);
            });

            item.addEventListener('touchmove', function() {
                clearTimeout(pressTimer);
            });
        });
    }

    function showContextMenu(e, liElement) {
        currentFileId = liElement.dataset.fileId;
        currentFileUrl = liElement.dataset.fileUrl;

        const menu = document.getElementById('contextMenu');
        if (!menu) return;

        let x, y;
        if (e.touches && e.touches.length > 0) {
            x = e.touches[0].clientX;
            y = e.touches[0].clientY;
        } else {
            x = e.clientX;
            y = e.clientY;
        }

        menu.style.display = 'block';
        menu.style.left = x + 'px';
        menu.style.top = y + 'px';

        const rect = menu.getBoundingClientRect();
        if (rect.right > window.innerWidth) {
            menu.style.left = (window.innerWidth - rect.width - 5) + 'px';
        }
        if (rect.bottom > window.innerHeight) {
            menu.style.top = (window.innerHeight - rect.height - 5) + 'px';
        }
    }

    // Close menu when clicking outside
    document.addEventListener('click', function(e) {
        const menu = document.getElementById('contextMenu');
        if (menu && !e.target.closest('.context-menu') && !e.target.closest('li[data-file-id]')) {
            menu.style.display = 'none';
        }
    });

	// Inside setupContextMenu() function

	// Download option
	const downloadOption = document.getElementById('downloadOption');
	if (downloadOption) {
	    downloadOption.addEventListener('click', function() {
	        if (currentFileId) {
	            // Use absolute path for download URL
	            const downloadUrl = '/download-file?id=' + encodeURIComponent(currentFileId);
	            window.open(downloadUrl, '_blank');
	            showToast('Download started', 'success');
	        }
	        document.getElementById('contextMenu').style.display = 'none';
	    });
	}

	// Delete option
	const deleteOption = document.getElementById('deleteOption');
	if (deleteOption) {
	    deleteOption.addEventListener('click', function() {
	        if (currentFileId && confirm('Are you sure you want to delete this file?')) {
	            deleteFile(currentFileId);
	        }
	        document.getElementById('contextMenu').style.display = 'none';
	    });
	}

	function deleteFile(fileId) {
	    try {
	        const csrfTokenField = document.querySelector('input[name="csrfToken"]');
	        const csrfToken = csrfTokenField ? csrfTokenField.value : '';
	        const xhr = new XMLHttpRequest();
	        // Use absolute path for delete URL
	        xhr.open('POST', '/delete-file', true);
	        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	        if (csrfToken) {
	            xhr.setRequestHeader('X-CSRF-Token', csrfToken);
	        }
	        xhr.onreadystatechange = function() {
	            if (xhr.readyState === 4) {
	                if (xhr.status === 200) {
	                    showToast('File deleted successfully', 'success');
	                    const fileElement = document.querySelector('li[data-file-id="' + fileId + '"]');
	                    if (fileElement) fileElement.remove();
	                    handleFileChange();
	                } else {
	                    showToast('Failed to delete file', 'error');
	                }
	            }
	        };
	        xhr.send('id=' + encodeURIComponent(fileId));
	    } catch (error) {
	        showToast('Error deleting file', 'error');
	        console.error('Delete error:', error);
	    }
	}

}

// Initialize the app when DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check for toast message from server
    if (typeof initialToastMessage !== 'undefined' && typeof initialToastType !== 'undefined') {
        showToast(initialToastMessage, initialToastType);
    }
});