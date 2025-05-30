CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    cloud_url VARCHAR(512) NOT NULL,
    category VARCHAR(100) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_id VARCHAR(255),
    file_size BIGINT
);