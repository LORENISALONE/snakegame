-- Tạo database cho game Snake
CREATE DATABASE IF NOT EXISTS snake_game;
USE snake_game;

-- Tạo bảng lưu thông tin người chơi
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    score INT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_score (score DESC),
    INDEX idx_difficulty (difficulty)
);

-- Tạo bảng lưu top điểm theo độ khó
CREATE TABLE IF NOT EXISTS high_scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    score INT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_difficulty_player (difficulty, player_name),
    INDEX idx_difficulty_score (difficulty, score DESC)
);

-- Insert một số dữ liệu mẫu
INSERT INTO players (player_name, score, difficulty) VALUES
('Player1', 150, 'EASY'),
('Player2', 200, 'MEDIUM'),
('Player3', 300, 'HARD'),
('Player4', 120, 'EASY'),
('Player5', 250, 'MEDIUM');
