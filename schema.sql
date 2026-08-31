-- MySQL Schema for Core Java Chat Application Database Integration
CREATE DATABASE IF NOT EXISTS chat_app_db;

USE chat_app_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
