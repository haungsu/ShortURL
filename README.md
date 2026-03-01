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
CREATE DATABASE IF NOT EXISTS short_url_pro 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE short_url_pro;

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
                             PRIMARY KEY (id),
                             UNIQUE INDEX idx_short_code (short_code) USING BTREE,
                             INDEX idx_name (name) USING BTREE,
                             INDEX idx_status (status) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

