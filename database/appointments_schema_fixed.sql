-- =============================================
-- Mentra App - Appointments & Counselor Schema
-- Run this SQL in phpMyAdmin or MySQL client
-- THIS MATCHES YOUR EXISTING DATABASE STRUCTURE
-- =============================================

USE mentra_db;

-- =============================================
-- 1. Add ROLE_COUNSELOR to roles table
-- =============================================
INSERT INTO roles (role_name) VALUES ('ROLE_COUNSELOR')
ON DUPLICATE KEY UPDATE role_name = 'ROLE_COUNSELOR';

-- =============================================
-- 2. Create sample counselor accounts
-- =============================================
-- First, insert the users
INSERT INTO users (matric_no, full_name, email, password_hash, is_enabled, created_at, dark_mode, font_size, language) 
VALUES ('COUNSELOR001', 'Dr. Sarah Ahmad', 'counselor@mentra.com', 'counselor123', true, NOW(), false, 'medium', 'en')
ON DUPLICATE KEY UPDATE email = 'counselor@mentra.com';

INSERT INTO users (matric_no, full_name, email, password_hash, is_enabled, created_at, dark_mode, font_size, language) 
VALUES ('COUNSELOR002', 'Dr. Rahman Ismail', 'rahman@mentra.com', 'counselor123', true, NOW(), false, 'medium', 'en')
ON DUPLICATE KEY UPDATE email = 'rahman@mentra.com';

-- Assign ROLE_COUNSELOR to these users
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id 
FROM users u, roles r 
WHERE u.email = 'counselor@mentra.com' AND r.role_name = 'ROLE_COUNSELOR';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id 
FROM users u, roles r 
WHERE u.email = 'rahman@mentra.com' AND r.role_name = 'ROLE_COUNSELOR';

-- =============================================
-- 3. Counselor Availability Table
-- =============================================
CREATE TABLE IF NOT EXISTS counselor_availability (
    availability_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counselor_id BIGINT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_counselor_day (counselor_id, day_of_week)
);

-- =============================================
-- 4. Appointments Table
-- =============================================
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    counselor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') DEFAULT 'PENDING',
    reason VARCHAR(500),
    notes TEXT,
    student_notes TEXT,
    counselor_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_student (student_id),
    INDEX idx_counselor (counselor_id),
    INDEX idx_status (status)
);

-- =============================================
-- 5. Time Slots Table (for predefined slots)
-- =============================================
CREATE TABLE IF NOT EXISTS time_slots (
    slot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_label VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE
);

-- Insert default time slots (1-hour sessions)
INSERT INTO time_slots (start_time, end_time, slot_label) VALUES
('09:00:00', '10:00:00', '9:00 AM - 10:00 AM'),
('10:00:00', '11:00:00', '10:00 AM - 11:00 AM'),
('11:00:00', '12:00:00', '11:00 AM - 12:00 PM'),
('14:00:00', '15:00:00', '2:00 PM - 3:00 PM'),
('15:00:00', '16:00:00', '3:00 PM - 4:00 PM'),
('16:00:00', '17:00:00', '4:00 PM - 5:00 PM')
ON DUPLICATE KEY UPDATE slot_label = VALUES(slot_label);

-- =============================================
-- 6. Default availability for counselors (Mon-Fri)
-- =============================================
INSERT IGNORE INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT u.user_id, 'MONDAY', '09:00:00', '17:00:00', TRUE 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';

INSERT IGNORE INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT u.user_id, 'TUESDAY', '09:00:00', '17:00:00', TRUE 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';

INSERT IGNORE INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT u.user_id, 'WEDNESDAY', '09:00:00', '17:00:00', TRUE 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';

INSERT IGNORE INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT u.user_id, 'THURSDAY', '09:00:00', '17:00:00', TRUE 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';

INSERT IGNORE INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT u.user_id, 'FRIDAY', '09:00:00', '17:00:00', TRUE 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';

-- =============================================
-- 7. Verify setup
-- =============================================
SELECT 'Tables Created Successfully!' AS Status;
SELECT 'Counselor Accounts:' AS Info;
SELECT u.user_id, u.full_name, u.email, r.role_name 
FROM users u 
JOIN user_roles ur ON u.user_id = ur.user_id 
JOIN roles r ON ur.role_id = r.role_id 
WHERE r.role_name = 'ROLE_COUNSELOR';
