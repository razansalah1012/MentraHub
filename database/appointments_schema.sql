-- =============================================
-- Mentra App - Appointments & Counselor Schema
-- Run this SQL in phpMyAdmin or MySQL client
-- =============================================

USE mentra_db;

-- =============================================
-- 1. Add counselor role to existing users table
-- =============================================
-- The 'role' column should already exist. Counselors will have role = 'counselor'

-- Create a sample counselor account
INSERT INTO users (full_name, email, password, role, dark_mode, font_size, language) 
VALUES ('Dr. Sarah Ahmad', 'counselor@mentra.com', 'counselor123', 'counselor', false, 'medium', 'en');

INSERT INTO users (full_name, email, password, role, dark_mode, font_size, language) 
VALUES ('Dr. Rahman Ismail', 'rahman@mentra.com', 'counselor123', 'counselor', false, 'medium', 'en');

-- =============================================
-- 2. Counselor Availability Table
-- =============================================
CREATE TABLE IF NOT EXISTS counselor_availability (
    availability_id INT AUTO_INCREMENT PRIMARY KEY,
    counselor_id INT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_counselor_day (counselor_id, day_of_week)
);

-- Default availability for counselors (Monday to Friday, 9 AM to 5 PM)
INSERT INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT user_id, 'MONDAY', '09:00:00', '17:00:00', TRUE FROM users WHERE role = 'counselor';

INSERT INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT user_id, 'TUESDAY', '09:00:00', '17:00:00', TRUE FROM users WHERE role = 'counselor';

INSERT INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT user_id, 'WEDNESDAY', '09:00:00', '17:00:00', TRUE FROM users WHERE role = 'counselor';

INSERT INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT user_id, 'THURSDAY', '09:00:00', '17:00:00', TRUE FROM users WHERE role = 'counselor';

INSERT INTO counselor_availability (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT user_id, 'FRIDAY', '09:00:00', '17:00:00', TRUE FROM users WHERE role = 'counselor';

-- =============================================
-- 3. Appointments Table
-- =============================================
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    counselor_id INT NOT NULL,
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
-- 4. Time Slots Table (for predefined slots)
-- =============================================
CREATE TABLE IF NOT EXISTS time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
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
('16:00:00', '17:00:00', '4:00 PM - 5:00 PM');

-- =============================================
-- 5. Verify setup
-- =============================================
SELECT 'Counselor Accounts Created:' AS Info;
SELECT user_id, full_name, email, role FROM users WHERE role = 'counselor';

SELECT 'Counselor Availability Set:' AS Info;
SELECT ca.counselor_id, u.full_name, ca.day_of_week, ca.start_time, ca.end_time 
FROM counselor_availability ca 
JOIN users u ON ca.counselor_id = u.user_id
ORDER BY ca.counselor_id, FIELD(ca.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY');

SELECT 'Time Slots Available:' AS Info;
SELECT * FROM time_slots;
