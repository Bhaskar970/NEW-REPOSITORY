-- ============================================================
--  Gym Management System — MySQL Schema
--  Run this file once to initialise the database
-- ============================================================

CREATE DATABASE IF NOT EXISTS gym_management;
USE gym_management;

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    phone       VARCHAR(15)         NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        ENUM('member','admin') DEFAULT 'member',
    created_at  TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ── Gym Slots ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS slots (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    slot_name   VARCHAR(100)        NOT NULL,
    start_time  TIME                NOT NULL,
    end_time    TIME                NOT NULL,
    max_capacity INT                NOT NULL DEFAULT 20,
    slot_date   DATE                NOT NULL,
    created_at  TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_slot (slot_name, slot_date)
);

-- ── Bookings ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT                 NOT NULL,
    slot_id     INT                 NOT NULL,
    status      ENUM('confirmed','cancelled') DEFAULT 'confirmed',
    booked_at   TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    UNIQUE KEY uq_booking (user_id, slot_id)          -- one booking per user per slot
);

-- ── Seed: Admin account ───────────────────────────────────────
-- Password stored as plain text here for demo; the Java layer
-- uses BCrypt — update after first login or hash with the app.
INSERT IGNORE INTO users (name, email, phone, password, role)
VALUES ('Admin', 'admin@gym.com', '0000000000', 'admin123', 'admin');

-- ── Seed: Sample slots for today and the next 7 days ─────────
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS seed_slots()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 8 DO
        INSERT IGNORE INTO slots (slot_name, start_time, end_time, max_capacity, slot_date) VALUES
            ('Morning Batch',   '06:00:00', '08:00:00', 20, DATE_ADD(CURDATE(), INTERVAL i DAY)),
            ('Afternoon Batch', '12:00:00', '14:00:00', 20, DATE_ADD(CURDATE(), INTERVAL i DAY)),
            ('Evening Batch',   '17:00:00', '19:00:00', 25, DATE_ADD(CURDATE(), INTERVAL i DAY)),
            ('Night Batch',     '20:00:00', '22:00:00', 15, DATE_ADD(CURDATE(), INTERVAL i DAY));
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_slots();
