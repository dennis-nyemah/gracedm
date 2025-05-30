package org.gihdm.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileTypeUtilTest {
    
    @Test
    void getFileType_ReturnsCorrectExtension() {
        assertEquals("pdf", FileTypeUtil.getFileType("document.pdf"));
        assertEquals("jpg", FileTypeUtil.getFileType("image.jpg"));
        assertEquals("mp4", FileTypeUtil.getFileType("video.mp4"));
    }

    @Test
    void getFileType_ReturnsUnknownForInvalidFiles() {
        assertEquals("unknown", FileTypeUtil.getFileType("noextension"));
        assertEquals("unknown", FileTypeUtil.getFileType(null));
    }

    @Test
    void isImage_IdentifiesImageTypes() {
        assertTrue(FileTypeUtil.isImage("jpg"));
        assertTrue(FileTypeUtil.isImage("png"));
        assertTrue(FileTypeUtil.isImage("jpeg"));
        assertTrue(FileTypeUtil.isImage("webp"));
        assertTrue(FileTypeUtil.isImage("gif"));
        assertFalse(FileTypeUtil.isImage("mp4"));
        assertFalse(FileTypeUtil.isImage("docx"));
    }

    @Test
    void isVideo_IdentifiesVideoTypes() {
        assertTrue(FileTypeUtil.isVideo("mp4"));
        assertTrue(FileTypeUtil.isVideo("mov"));
        assertTrue(FileTypeUtil.isVideo("avi"));
        assertTrue(FileTypeUtil.isVideo("mkv"));
        assertTrue(FileTypeUtil.isVideo("webm"));
        assertFalse(FileTypeUtil.isVideo("jpg"));
        assertFalse(FileTypeUtil.isVideo("pdf"));
    }
}