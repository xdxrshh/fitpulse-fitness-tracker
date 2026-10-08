-- =====================================================================
-- FitPulse Fitness Tracking System — Database Schema & Seed Data
-- Database: fitpulse_db
-- Target: MySQL 5.7+ / 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS fitpulse_db;
USE fitpulse_db;

-- 1. Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS meal_logs;
DROP TABLE IF EXISTS fitness_goals;
DROP TABLE IF EXISTS workouts;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS trainers;
DROP TABLE IF EXISTS users;

-- 2. Base Users Table (Encapsulates credentials and roles)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Trainers Subtype Table (Inheritance mapping)
CREATE TABLE trainers (
    user_id INT PRIMARY KEY,
    specialization VARCHAR(100) NOT NULL,
    certification VARCHAR(100) NOT NULL,
    CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Members Subtype Table (Inheritance mapping)
CREATE TABLE members (
    user_id INT PRIMARY KEY,
    weight_kg DECIMAL(5, 2) NOT NULL,
    height_cm DECIMAL(5, 2) NOT NULL,
    fitness_level VARCHAR(30) DEFAULT 'BEGINNER',
    trainer_id INT NULL,
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_members_trainer FOREIGN KEY (trainer_id) 
        REFERENCES trainers (user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Workouts Table (Polymorphic hierarchy: Cardio & Strength)
CREATE TABLE workouts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    workout_type VARCHAR(20) NOT NULL, -- 'CARDIO', 'STRENGTH'
    workout_date DATETIME NOT NULL,
    duration_min INT NOT NULL,
    calories_burned DOUBLE NOT NULL,
    -- Specific to CardioWorkout
    distance_km DOUBLE NULL,
    avg_heart_rate INT NULL,
    -- Specific to StrengthWorkout
    total_weight_kg DOUBLE NULL,
    sets_count INT NULL,
    reps_count INT NULL,
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_workout_user_date (user_id, workout_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Fitness Goals Table (Milestones & Target Tracking)
CREATE TABLE fitness_goals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    goal_type VARCHAR(40) NOT NULL, -- 'CALORIE_BURN', 'WORKOUT_COUNT', 'WEIGHT_LOSS'
    target_value DOUBLE NOT NULL,
    current_value DOUBLE DEFAULT 0.0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- 'IN_PROGRESS', 'COMPLETED', 'ABANDONED'
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_goal_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Meal Logs Table (Daily Nutrition & Calorie Tracking)
CREATE TABLE meal_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    meal_name VARCHAR(100) NOT NULL,
    meal_type VARCHAR(20) NOT NULL, -- 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'
    calories INT NOT NULL,
    protein_g DOUBLE DEFAULT 0.0,
    carbs_g DOUBLE DEFAULT 0.0,
    fat_g DOUBLE DEFAULT 0.0,
    logged_at DATETIME NOT NULL,
    CONSTRAINT fk_meals_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_meals_user_date (user_id, logged_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Audit Logs Table (Transaction management audit trails)
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- SEED DATA (For Immediate Demonstration and Testing)
-- Passwords are SHA-256 hashed ('password123' -> 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f')
-- =====================================================================

INSERT INTO users (id, name, email, password_hash, role) VALUES
(1, 'Alex Mercer', 'alex@fitpulse.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'TRAINER'),
(2, 'Sarah Connor', 'sarah@fitpulse.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER'),
(3, 'John Doe', 'john@fitpulse.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER');

INSERT INTO trainers (user_id, specialization, certification) VALUES
(1, 'High Intensity Interval Training & Strength', 'NASM-CPT #88291');

INSERT INTO members (user_id, weight_kg, height_cm, fitness_level, trainer_id) VALUES
(2, 64.5, 168.0, 'INTERMEDIATE', 1),
(3, 78.0, 180.5, 'BEGINNER', 1);

-- Workouts: Polymorphic Cardio and Strength entries
INSERT INTO workouts (user_id, workout_type, workout_date, duration_min, calories_burned, distance_km, avg_heart_rate, total_weight_kg, sets_count, reps_count, notes) VALUES
(2, 'CARDIO', DATE_SUB(NOW(), INTERVAL 3 DAY), 45, 410.0, 6.2, 148, NULL, NULL, NULL, 'Morning lakeside run'),
(2, 'STRENGTH', DATE_SUB(NOW(), INTERVAL 2 DAY), 60, 380.0, NULL, NULL, 3200.0, 16, 10, 'Full body compound lifts'),
(2, 'CARDIO', DATE_SUB(NOW(), INTERVAL 1 DAY), 30, 290.0, 4.5, 142, NULL, NULL, NULL, 'Treadmill interval sprints'),
(3, 'CARDIO', DATE_SUB(NOW(), INTERVAL 2 DAY), 25, 210.0, 3.0, 135, NULL, NULL, NULL, 'Warm-up jog'),
(3, 'STRENGTH', DATE_SUB(NOW(), INTERVAL 1 DAY), 50, 320.0, NULL, NULL, 2400.0, 12, 12, 'Leg day & core');

-- Goals
INSERT INTO fitness_goals (user_id, goal_type, target_value, current_value, start_date, end_date, status) VALUES
(2, 'CALORIE_BURN', 2500.0, 1080.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS'),
(2, 'WORKOUT_COUNT', 5.0, 3.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS'),
(3, 'CALORIE_BURN', 2000.0, 530.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'IN_PROGRESS');

-- Nutrition Logs
INSERT INTO meal_logs (user_id, meal_name, meal_type, calories, protein_g, carbs_g, fat_g, logged_at) VALUES
(2, 'Oatmeal & Whey Protein Shake', 'BREAKFAST', 420, 34.0, 48.0, 8.0, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 'Grilled Chicken Quinoa Bowl', 'LUNCH', 650, 48.0, 62.0, 16.0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(3, 'Avocado Toast with Poached Eggs', 'BREAKFAST', 480, 22.0, 38.0, 24.0, DATE_SUB(NOW(), INTERVAL 3 HOUR));
