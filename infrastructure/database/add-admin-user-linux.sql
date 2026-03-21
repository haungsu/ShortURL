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