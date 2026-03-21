-- ShortURLPro MySQL数据库初始化脚本 (Linux版本)
-- 适用于生产环境部署

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS short_url_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE short_url_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    nickname VARCHAR(50) NOT NULL COMMENT '用户昵称',
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-未删，1-已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建用户角色关联表（ElementCollection映射）
CREATE TABLE IF NOT EXISTS t_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    PRIMARY KEY (user_id, role_code),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建短链接表
CREATE TABLE IF NOT EXISTS t_short_url (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '短链接名称（用于展示）',
    original_url TEXT NOT NULL COMMENT '原始长链接',
    short_code VARCHAR(10) NOT NULL UNIQUE COMMENT '短码（6位Base62）',
    status ENUM('ENABLED', 'DISABLED') NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
    click_count BIGINT NOT NULL DEFAULT 0 COMMENT '点击次数',
    app_id VARCHAR(100) COMMENT '应用ID（扩展字段）',
    create_user_id BIGINT COMMENT '创建者用户ID（关联t_user表）',
    expires_at DATETIME COMMENT '过期时间（NULL表示永久有效）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_short_code (short_code),
    INDEX idx_create_user_id (create_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (create_user_id) REFERENCES t_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短链接表';

-- 创建数据库用户并授权（生产环境建议）
-- 注意：请根据实际需要修改用户名和密码
CREATE USER IF NOT EXISTS 'short_url_db'@'localhost' IDENTIFIED BY 'your_secure_password_here';
CREATE USER IF NOT EXISTS 'short_url_db'@'%' IDENTIFIED BY 'your_secure_password_here';

-- 授予完整的数据库权限
GRANT ALL PRIVILEGES ON short_url_db.* TO 'short_url_db'@'localhost';
GRANT ALL PRIVILEGES ON short_url_db.* TO 'short_url_db'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建结果
SELECT 'Database initialization completed successfully!' AS message;
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'short_url_db';

-- ShortURLPro 管理员用户导入脚本 (Linux版本)
-- 在执行此脚本前，请确保已经运行了 init-mysql-linux.sql

USE short_url_db;

-- 插入管理员用户
-- 默认密码：admin123 (BCrypt加密后的值)
INSERT INTO t_user (username, password, nickname, is_deleted, created_at, updated_at) VALUES 
('admin', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'admin1', 0, NOW(), NOW()),
('testuser', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'testuser1', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    nickname = VALUES(nickname),
    updated_at = NOW();

-- 为管理员用户分配角色
-- 清除现有角色（避免重复）
DELETE FROM t_user_role WHERE user_id IN (
    SELECT id FROM t_user WHERE username IN ('admin', 'testuser')
);

-- 分配角色
INSERT INTO t_user_role (user_id, role_code) 
SELECT u.id, 'ROLE_ADMIN' 
FROM t_user u 
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);

INSERT INTO t_user_role (user_id, role_code) 
SELECT u.id, 'ROLE_USER' 
FROM t_user u 
WHERE u.username = 'testuser'
ON DUPLICATE KEY UPDATE role_code = VALUES(role_code);

-- 验证插入结果
SELECT 'Admin user import completed!' AS message;
SELECT u.id, u.username, u.nickname, ur.role_code 
FROM t_user u 
LEFT JOIN t_user_role ur ON u.id = ur.user_id 
WHERE u.username IN ('admin', 'testuser')
ORDER BY u.username, ur.role_code;

-- 显示所有用户信息
SELECT 
    id,
    username,
    nickname,
    is_deleted,
    created_at,
    updated_at
FROM t_user 
ORDER BY id;