CREATE TABLE applications (
    id VARCHAR2(255) NOT NULL PRIMARY KEY,
    completed_at TIMESTAMP NULL,
    data clob NOT NULL
)