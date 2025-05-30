package org.gihdm.model;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void constructor_SetsAllFieldsCorrectly() {
        Document doc = new Document("Invitation.pdf", "http://cloud.com/sermon", 
                                  "Letters", "CLOUDINARY", "pastor@church.org");
        
        assertEquals("Invitation.pdf", doc.getTitle());
        assertEquals("Letters", doc.getCategory());
        assertNotNull(doc.getUploadedAt());
        assertTrue(doc.getUploadedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void setters_UpdateFieldsCorrectly() {
        Document doc = new Document();
        doc.setId("123");
        doc.setFileSize(1024L);
        
        assertEquals("123", doc.getId());
        assertEquals(1024L, doc.getFileSize());
    }
}