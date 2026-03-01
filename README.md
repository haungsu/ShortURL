# ShortURL Pro

## 项目简介

ShortURL Pro 是一个基于 Spring Boot 的短链接服务系统，支持链接缩短、访问统计、用户管理等功能。

## 本地部署

### MySQL配置

1.进sql

先确保你已经安装了MySQL

运行以下命令
```bash
mysql -u root -p
```
输入你的SQL密码

2. 运行以下代码
```sql
DROP DATABASE IF EXISTS short_url_db;

CREATE DATABASE IF NOT EXISTS short_url_db 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE short_url_db;

DROP TABLE IF EXISTS t_user;
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

DROP TABLE IF EXISTS t_user_role;
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

DROP TABLE IF EXISTS t_short_url;
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

INSERT INTO t_user (username, password, nickname)
VALUES ('admin', '$2a$10$7wL3hY6u0e9G5x8z2s7d1a9b8c7v6b5n4m3l2k1j0', 'admin1');

INSERT INTO t_user_role (user_id, role_code) VALUES (1, 'ROLE_ADMIN');

INSERT INTO t_user (username, password, nickname)
VALUES ('user', '$2a$10$7wL3hY6u0e9G5x8z2s7d1a9b8c7v6b5n4m3l2k1j0', 'user1');

INSERT INTO t_user_role (user_id, role_code) VALUES (2, 'ROLE_USER');
```

以上密码对应明文为 123456
