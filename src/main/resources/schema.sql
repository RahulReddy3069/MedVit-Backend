-- Database Initialization Script for MySQL (Doctor-Patient Consultation App)
CREATE DATABASE IF NOT EXISTS hospital_consultation_db;
USE hospital_consultation_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'Patient', 'Doctor', 'Admin'
    subscription VARCHAR(20) DEFAULT 'Standard', -- 'Standard', 'Premium'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Doctors Table (With Spatial Geospatial coordinates)
CREATE TABLE IF NOT EXISTS doctors (
    id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    location POINT NOT NULL, -- Point coordinates (X: Lat, Y: Lng)
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    SPATIAL INDEX (location) -- Spatial index for high performance proximity searches
) ENGINE=InnoDB;

-- 3. Consultation Sessions Table (Tracks matchmaking lifecycle)
CREATE TABLE IF NOT EXISTS consultation_sessions (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending', -- 'Pending', 'Active', 'Rejected', 'Expired', 'Completed'
    symptom_description TEXT NOT NULL,
    request_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_timestamp TIMESTAMP NULL,
    timer_expires_at TIMESTAMP NOT NULL, -- Calculated as request_timestamp + 5 mins
    ended_at TIMESTAMP NULL,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (doctor_id) REFERENCES users(id)
);

-- 4. Chat Messages Table (Zero-Knowledge Isolation)
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message_text_encrypted TEXT NOT NULL, -- Encrypted client-side to enforce privacy
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES consultation_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 5. Message Attachments Table
CREATE TABLE IF NOT EXISTS message_attachments (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    uploader_id VARCHAR(36) NOT NULL,
    s3_url_encrypted TEXT NOT NULL, -- S3 URL encrypted client-side
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES consultation_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (uploader_id) REFERENCES users(id)
);

-- 6. Admin Audit Logs (Strict metadata logging only, no chat text logs)
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    admin_user_id VARCHAR(36) NOT NULL,
    action_performed VARCHAR(255) NOT NULL,
    target_session_id VARCHAR(36) NULL,
    ip_address VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES users(id)
);
