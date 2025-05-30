package org.gihdm.repository;

import org.gihdm.config.HikariCPDataSource;
import org.gihdm.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentRepository implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DocumentRepository.class);
    private Connection conn;
    private boolean isTransactionActive = false;

    public DocumentRepository() throws SQLException {
        this.conn = HikariCPDataSource.getConnection();
        this.conn.setAutoCommit(true); // Default to auto-commit mode
        logger.debug("Acquired new connection: {}", conn);
    }

    // Transaction management methods
    public void beginTransaction() throws SQLException {
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
            isTransactionActive = true;
            logger.debug("Transaction started");
        }
    }

    public void commit() throws SQLException {
        if (isTransactionActive) {
            conn.commit();
            conn.setAutoCommit(true);
            isTransactionActive = false;
            logger.debug("Transaction committed");
        }
    }

    public void rollback() {
        if (isTransactionActive) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                isTransactionActive = false;
                logger.debug("Transaction rolled back");
            } catch (SQLException e) {
                logger.error("Error during rollback", e);
            }
        }
    }

    // CRUD methods
    public Document findById(String id) throws SQLException {
        String sql = "SELECT * FROM documents WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(id));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? buildDocument(rs) : null;
            }
        } catch (SQLException e) {
            logger.error("Error finding document by ID: {}", id, e);
            rollback();
            throw e;
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM documents WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(id));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting document: {}", id, e);
            rollback();
            throw e;
        }
    }

    public void save(String fileName, String fileUrl, String fileId, String category,
                   String storageProvider, String uploadedBy) throws SQLException {
        String sql = """
            INSERT INTO documents (id, title, cloud_url, file_id, category, 
                                 storage_provider, uploaded_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, UUID.randomUUID());
            stmt.setString(2, fileName);
            stmt.setString(3, fileUrl);
            stmt.setString(4, fileId);
            stmt.setString(5, category);
            stmt.setString(6, storageProvider);
            stmt.setString(7, uploadedBy);
            
            int affected = stmt.executeUpdate();
            logger.debug("Inserted {} rows into documents table", affected);
        } catch (SQLException e) {
            logger.error("Failed to save document metadata", e);
            rollback();
            throw e;
        }
    }

    public List<Document> findAll() throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY category, uploaded_at DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                documents.add(buildDocument(rs));
            }
            return documents;
        } catch (SQLException e) {
            logger.error("Error fetching all documents", e);
            rollback();
            throw e;
        }
    }

    private Document buildDocument(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setId(rs.getString("id"));
        doc.setTitle(rs.getString("title"));
        doc.setCloudUrl(rs.getString("cloud_url"));
        doc.setFileId(rs.getString("file_id"));
        doc.setCategory(rs.getString("category"));
        doc.setStorageProvider(rs.getString("storage_provider"));
        doc.setUploadedBy(rs.getString("uploaded_by"));
        doc.setUploadedAt(rs.getObject("uploaded_at", LocalDateTime.class));
        doc.setFileSize(rs.getLong("file_size"));
        return doc;
    }

    @Override
    public void close() {
        try {
            if (conn != null) {
                rollback(); // Ensure any active transaction is rolled back
                if (!conn.isClosed()) {
                    conn.close();
                    logger.debug("Connection closed successfully");
                }
            }
        } catch (SQLException e) {
            logger.error("Error closing connection", e);
        } finally {
            conn = null; // Help GC
        }
    }
}