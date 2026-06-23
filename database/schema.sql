-- Waste Reporting Database Schema
-- Compatible with XAMPP MySQL / MariaDB

CREATE DATABASE IF NOT EXISTS wastereporting;
USE wastereporting;

-- 1. Users Table (Citizens, Supervisors, Admins)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL DEFAULT 'citizen', -- 'citizen', 'supervisor', 'admin'
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NULL,
    dob DATE NULL,
    gender VARCHAR(20) NULL,
    address TEXT NULL,
    city VARCHAR(100) NULL,
    country VARCHAR(100) NULL,
    profile_image_url VARCHAR(500) NULL,
    eco_points INT NOT NULL DEFAULT 0,
    password_hash VARCHAR(255) NOT NULL,
    is_active INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Supervisors Table (extends users table)
CREATE TABLE IF NOT EXISTS supervisors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_id VARCHAR(100) NOT NULL UNIQUE,
    assigned_area VARCHAR(255) NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    coverage_radius DECIMAL(5, 2) DEFAULT 10.00 NULL, -- coverage radius in kilometers
    department VARCHAR(100) NULL,
    performance_score INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Issue Reports Table
CREATE TABLE IF NOT EXISTS issue_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    citizen_id INT NOT NULL,
    assigned_supervisor_id INT NULL,
    title VARCHAR(255) NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address VARCHAR(500) NULL,
    location VARCHAR(255) NULL,
    image_url VARCHAR(500) NULL,
    completion_image_url VARCHAR(500) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Pending', -- 'Pending', 'In Progress', 'Completed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (citizen_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_supervisor_id) REFERENCES supervisors(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Issue History Table
CREATE TABLE IF NOT EXISTS issue_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_by INT NOT NULL,
    remarks TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (issue_id) REFERENCES issue_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read_status TINYINT(1) NOT NULL DEFAULT 0, -- 0 for Unread, 1 for Read
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Activity Logs Table
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'login', 'create', 'update', 'delete', 'report', 'security'
    details TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. OTP Verification Table
CREATE TABLE IF NOT EXISTS otps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_supervisors_employee ON supervisors(employee_id);
CREATE INDEX idx_issues_citizen ON issue_reports(citizen_id);
CREATE INDEX idx_issues_status ON issue_reports(status);
CREATE INDEX idx_activity_logs_created ON activity_logs(created_at);
CREATE INDEX idx_otps_email ON otps(email);

-- 8. Insert Default Admin User (Password: Admin@123)
INSERT INTO users (role, full_name, email, phone, dob, is_active, password_hash)
VALUES ('admin', 'EcoCollect Admin', 'admin@ecocollect.city', '+1 (555) 000-0001', '1990-01-01', 1, '$2b$12$aj1.3HRlCzX4PLk1HgQfseunmFBGK4iw2BBN.zylVGtd3wGrEN/46')
ON DUPLICATE KEY UPDATE 
    role='admin', 
    full_name='EcoCollect Admin', 
    phone='+1 (555) 000-0001', 
    dob='1990-01-01', 
    is_active=1,
    password_hash='$2b$12$aj1.3HRlCzX4PLk1HgQfseunmFBGK4iw2BBN.zylVGtd3wGrEN/46';
