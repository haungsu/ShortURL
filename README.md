# ShortURL Pro

## 项目简介
ShortURL Pro 是一个基于 Spring Boot 的短链接服务系统，支持链接缩短、访问统计、用户管理等功能。

## 部署方式
支持**本地部署**和**Docker部署**两种方式，Docker部署无需配置本地Java/Maven/MySQL环境，推荐生产/测试环境使用。

### 初始账号信息
部署完成后可使用以下默认账号登录：
- 管理员账号：`admin`，密码：`123456`
- 普通用户账号：`user`，密码：`123456`

---

### 一、本地部署
#### 前置条件
1. 本地安装 JDK 21+、Maven 3.8+、MySQL 8.0+
2. 确保MySQL服务正常运行

#### MySQL配置
1. 登录MySQL数据库：
```bash
mysql -u root -p
```
输入你的MySQL密码后执行以下SQL脚本，完成数据库和表结构初始化：

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
VALUES ('admin', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'admin1');

INSERT INTO t_user_role (user_id, role_code) VALUES (1, 'ROLE_ADMIN');

INSERT INTO t_user (username, password, nickname)
VALUES ('user', '$2a$10$1hXKhzWk7.USvoddyOFOvOW9YbYJoQ7gEQv1CwPzty0I0Bkrpk63C', 'user1');

INSERT INTO t_user_role (user_id, role_code) VALUES (2, 'ROLE_USER');
```

#### 项目启动
1. 克隆项目到本地，修改 `application.properties` 中的MySQL连接信息或将你的MySQL账号密码设为环境变量：
```properties
spring.datasource.username=你的MySQL账号
spring.datasource.password=你的MySQL密码
```
2. 执行Maven打包并启动项目：
```bash
mvn clean package -DskipTests
java -jar target/*.jar
```
3. 访问 `http://localhost:8080` 即可使用系统。

---

### 二、Docker部署
#### 前置条件
1. 目标服务器安装 Docker 和 Docker Compose（推荐 Docker 20.10+、Docker Compose 2.0+）
2. 服务器开放 8080（应用）、3306（MySQL）端口（按需调整）

#### 方式1：Docker Compose 一键部署（推荐）
自动部署应用+MySQL，无需手动配置数据库，步骤如下：

1. 启动服务：
```bash
docker-compose up -d
```

2. 访问 `http://你的服务器IP:8080` 即可使用系统。


3. 查看服务日志
```bash
docker-compose logs -f short-url-app
```

#### 方式2：单独拉取DockerHub镜像部署（需已有MySQL）
若服务器已有MySQL（非Docker版），可直接拉取镜像运行：

1. 拉取镜像：
```bash
docker pull lattt/short-url-pro:0.0.1
```

2. 运行容器（替换以下参数为你的MySQL信息）：
```bash
docker run -d \
  --name short-url-pro \
  -p 8080:8080 \
  --restart=always \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://你的MySQLIP:3306/short_url_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=你的MySQL密码 \
  -e SHORT_URL_BASE_URL=http://你的服务器IP:8080 \
  lattt/short-url-pro:0.0.1
```
