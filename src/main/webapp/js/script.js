var menuToggle = document.getElementById('menuToggle');

var closeBtn = document.getElementById('closeBtn');

var sidebar = document.getElementById('sidebar');

var overlay = document.getElementById('overlay');

var uploadBtn = document.getElementById('uploadBtn');

var submitBtn = document.getElementById('submitBtn');

var fileInput = document.getElementById('fileInput');

var fileInfo = document.getElementById('fileInfo');

var selectedCategory = document.getElementById('selectedCategory');

var uploadForm = document.getElementById('uploadForm');

var uploadTitle = document.getElementById('upload-title');



// Sidebar toggling

menuToggle.addEventListener('click', function() {

sidebar.style.width = '300px';

overlay.classList.add('active');

document.body.style.overflow = 'hidden';

});



closeBtn.addEventListener('click', closeSidebar);

overlay.addEventListener('click', closeSidebar);



function closeSidebar() {

sidebar.style.width = '0';

overlay.classList.remove('active');

document.body.style.overflow = 'auto';

}



// Dropdown toggle functionality

var sidebarTitles = document.querySelectorAll('.sidebar-item-title');

for (var i = 0; i < sidebarTitles.length; i++) {

sidebarTitles[i].addEventListener('click', function(e) {

e.stopPropagation();

this.classList.toggle('active');

var submenu = this.nextElementSibling;

if (submenu) submenu.classList.toggle('active');

});

}



// Upload interaction

uploadBtn.addEventListener('click', function() {

fileInput.click();

});



fileInput.addEventListener('change', function() {

if (fileInput.files.length > 0) {

var names = [];

for (var i = 0; i < fileInput.files.length; i++) {

names.push(fileInput.files[i].name);

}

var currentCat = selectedCategory.value.replace(/_/g, ' ');

fileInfo.textContent = 'Uploading ' + fileInput.files.length + ' file(s) to ' + currentCat + ': ' + names.join(', ');

fileInfo.style.color = '#2196F3';

submitBtn.style.display = 'block';

} else {

fileInfo.textContent = 'No files selected';

fileInfo.style.color = '#666';

submitBtn.style.display = 'none';

}

});



// Drag & drop support

var uploadBox = document.querySelector('.upload-box');

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



// Form submission

uploadForm.addEventListener('submit', function(e) {

e.preventDefault();

var urlParams = new URLSearchParams(window.location.search);

var category = urlParams.get('category') ||

sessionStorage.getItem('lastCategory') ||

selectedCategory.value ||

'Letters';



selectedCategory.value = category;

this.submit();

});



// Toast messages

function showToast(message, type) {

type = type || 'success';

var toast = document.createElement('div');

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

var currentFileId = null;

var currentFileUrl = null;



var fileLinks = document.querySelectorAll('.file-link');

for (var i = 0; i < fileLinks.length; i++) {

fileLinks[i].addEventListener('click', function(e) {

e.preventDefault();

var li = this.closest('li');

if (li) window.open(li.dataset.fileUrl, '_blank');

});

}



var fileItems = document.querySelectorAll('li[data-file-id]');

for (var j = 0; j < fileItems.length; j++) {

fileItems[j].addEventListener('contextmenu', function(e) {

e.preventDefault();

showContextMenu(e, this);

});



var pressTimer;

fileItems[j].addEventListener('touchstart', function(e) {

pressTimer = setTimeout(function() {

e.preventDefault();

showContextMenu(e, this);

}.bind(this), 500);

});



fileItems[j].addEventListener('touchend', function() {

clearTimeout(pressTimer);

});

fileItems[j].addEventListener('touchmove', function() {

clearTimeout(pressTimer);

});

}



function showContextMenu(e, liElement) {

currentFileId = liElement.dataset.fileId;

currentFileUrl = liElement.dataset.fileUrl;



var menu = document.getElementById('contextMenu');

if (!menu) return;



var x, y;

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



var rect = menu.getBoundingClientRect();

if (rect.right > window.innerWidth) {

menu.style.left = (window.innerWidth - rect.width - 5) + 'px';

}

if (rect.bottom > window.innerHeight) {

menu.style.top = (window.innerHeight - rect.height - 5) + 'px';

}

}



document.addEventListener('click', function(e) {

var menu = document.getElementById('contextMenu');

if (menu && !e.target.closest('.context-menu') && !e.target.closest('li[data-file-id]')) {

menu.style.display = 'none';

}

});



var downloadOption = document.getElementById('downloadOption');

var deleteOption = document.getElementById('deleteOption');



if (downloadOption) {

downloadOption.addEventListener('click', function() {

if (currentFileId) {

var contextPath = '/' + window.location.pathname.split('/')[1];

var downloadUrl = contextPath + '/download-file?id=' + encodeURIComponent(currentFileId);

window.open(downloadUrl, '_blank');

showToast('Download started', 'success');

}



var menu = document.getElementById('contextMenu');

if (menu) menu.style.display = 'none';

});

}


if (deleteOption) {

deleteOption.addEventListener('click', function() {

if (currentFileId && confirm('Are you sure you want to delete this file?')) {

deleteFile(currentFileId);

}

var menu = document.getElementById('contextMenu');

if (menu) menu.style.display = 'none';

});

}



function deleteFile(fileId) {

try {

var csrfTokenField = document.querySelector('input[name="csrfToken"]');

var csrfToken = csrfTokenField ? csrfTokenField.value : '';

var xhr = new XMLHttpRequest();

var contextPath = '/' + window.location.pathname.split('/')[1];

xhr.open('POST', contextPath + '/delete-file', true);

xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

if (csrfToken) {

xhr.setRequestHeader('X-CSRF-Token', csrfToken);

}

xhr.onreadystatechange = function() {

if (xhr.readyState === 4) {

if (xhr.status === 200) {

showToast('File deleted successfully', 'success');

var fileElement = document.querySelector('li[data-file-id="' + fileId + '"]');

if (fileElement) fileElement.remove();

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



// Initialize

document.addEventListener('DOMContentLoaded', function() {

var urlParams = new URLSearchParams(window.location.search);

var currentCat = urlParams.get('category') || 'Letters';

selectedCategory.value = currentCat;



if (uploadTitle) {

uploadTitle.textContent = 'Upload to ' + currentCat.replace(/_/g, ' ');

}



setupContextMenu();

});





