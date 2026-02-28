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
COLLATE utf8mb4_unicode_ci;
```

