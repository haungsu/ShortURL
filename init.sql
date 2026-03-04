CREATE DATABASE IF NOT EXISTS short_url_db
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE short_url_db;

CREATE TABLE IF NOT EXISTS t_user (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_username (username) USING BTREE,
    INDEX idx_is_deleted (is_deleted) USING BTREE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS t_user_role (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           role_code VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id) USING BTREE,
    INDEX idx_role_code (role_code) USING BTREE,
    FOREIGN KEY (user_id) REFERENCES t_user (id) ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS t_short_url (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           name VARCHAR(50) NOT NULL,
    original_url VARCHAR(2000) NOT NULL,
    short_code VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    click_count BIGINT DEFAULT 0,
    app_id VARCHAR(32) NULL,
    expires_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_short_code (short_code) USING BTREE,
    INDEX idx_name (name) USING BTREE,
    INDEX idx_status (status) USING BTREE,
    INDEX idx_create_user_id (create_user_id) USING BTREE,
    FOREIGN KEY (create_user_id) REFERENCES t_user (id) ON UPDATE CASCADE ON DELETE SET NULL
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO t_user (username, password, nickname)
SELECT 'admin', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'admin1'
    WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'admin');

INSERT INTO t_user_role (user_id, role_code)
SELECT (SELECT id FROM t_user WHERE username = 'admin'), 'ROLE_ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM t_user_role WHERE role_code = 'ROLE_ADMIN');

INSERT INTO t_user (username, password, nickname)
SELECT 'user', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'user1'
    WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'user');

INSERT INTO t_user_role (user_id, role_code)
SELECT (SELECT id FROM t_user WHERE username = 'user'), 'ROLE_USER'
    WHERE NOT EXISTS (SELECT 1 FROM t_user_role WHERE role_code = 'ROLE_USER' AND user_id = (SELECT id FROM t_user WHERE username = 'user'));