-- 短链接服务数据库初始化脚本
-- 用于快速删除原有数据库并创建新数据库
-- 执行前请确保已备份重要数据

-- 删除现有数据库（如果存在）
DROP DATABASE IF EXISTS short_url_db;

-- 创建新的数据库
CREATE DATABASE IF NOT EXISTS short_url_db 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用新创建的数据库
USE short_url_db;

-- 删除用户表（如果存在）
DROP TABLE IF EXISTS t_user;

-- 创建用户表
CREATE TABLE t_user (
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

-- 删除用户角色表（如果存在）
DROP TABLE IF EXISTS t_user_role;

-- 创建用户角色表
CREATE TABLE t_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_code VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id) USING BTREE,
    INDEX idx_role_code (role_code) USING BTREE,
    FOREIGN KEY (user_id) REFERENCES t_user (id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 删除短链接表（如果存在）
DROP TABLE IF EXISTS t_short_url;

-- 创建短链接表
CREATE TABLE t_short_url (
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

-- 插入默认管理员用户
-- 密码: admin123 (已加密)
INSERT INTO t_user (username, password, nickname)
VALUES ('admin', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'admin1');

-- 为管理员用户分配角色
INSERT INTO t_user_role (user_id, role_code) VALUES (1, 'ROLE_ADMIN');

-- 插入默认普通用户
-- 密码: user123 (已加密)
INSERT INTO t_user (username, password, nickname)
VALUES ('user', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'user1');

-- 为普通用户分配角色
INSERT INTO t_user_role (user_id, role_code) VALUES (2, 'ROLE_USER');

-- 显示创建结果
SELECT 'Database initialization completed successfully!' AS result;
SELECT COUNT(*) AS user_count FROM t_user;
SELECT COUNT(*) AS role_count FROM t_user_role;
SELECT COUNT(*) AS short_url_count FROM t_short_url;