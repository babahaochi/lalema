CREATE DATABASE IF NOT EXISTS lalema DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lalema;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poop_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date VARCHAR(10) NOT NULL,
    time_hour INT NOT NULL DEFAULT 0,
    time_minute INT NOT NULL DEFAULT 0,
    amount VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    consistency VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    color VARCHAR(20) NOT NULL DEFAULT 'BROWN',
    smell VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    pain_level INT NOT NULL DEFAULT 0,
    blood TINYINT(1) NOT NULL DEFAULT 0,
    mucus TINYINT(1) NOT NULL DEFAULT 0,
    notes VARCHAR(500) DEFAULT '',
    local_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_date (user_id, date),
    INDEX idx_local_id (user_id, local_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    UNIQUE KEY uk_sender_receiver (sender_id, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    remark VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_friend (friend_id),
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
