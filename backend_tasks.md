# ShortURLPro 后端开发任务清单

## 项目概述
ShortURLPro 是一个短链接服务系统，提供短链接生成、跳转和访问统计功能。

## 核心功能模块

### 1. 短链接核心功能
- [ ] 短链接生成服务
- [ ] 302重定向跳转
- [ ] 访问统计计数
- [ ] 短链接状态管理（启用/禁用）

### 2. 数据管理
- [ ] 短链接数据存储
- [ ] 用户数据管理
- [ ] 访问日志记录

### 3. 系统管理
- [ ] 用户认证与授权
- [ ] 管理后台接口
- [ ] 系统配置管理

## 技术栈要求

### 开发环境
- **IDE**: IntelliJ IDEA
- **JDK**: Java 11 或更高版本
- **构建工具**: Maven
- **版本控制**: Git + GitHub

### 核心框架
- **Spring Boot**: 3.4.x 版本
- **Spring Web**: RESTful API 开发
- **Spring Security**: 安全认证
- **Spring Data JPA**: 数据持久化

### 数据库
- **MySQL**: 8.0 版本
- **数据库管理工具**: Navicat（可选）

## 环境搭建任务

### 1. 开发工具安装
- [ ] 安装 IntelliJ IDEA（推荐 Ultimate 版）
- [ ] 配置 JDK 环境
- [ ] 安装 Maven 并配置阿里云镜像
- [ ] 安装 Git 并配置用户信息

### 2. 数据库环境
- [ ] 安装 MySQL 8.0
- [ ] 配置 MySQL 服务
- [ ] 安装 Navicat（可选）
- [ ] 创建数据库 `short_url_db`

### 3. 版本控制
- [ ] 创建 GitHub 仓库
- [ ] 配置 Git 远程仓库
- [ ] 推送初始代码

## 项目结构规划

```
short-url-service/
├── src/main/java/com/example/shorturl/
│   ├── controller/           # 控制器层
│   ├── service/              # 服务层
│   ├── entity/               # 实体类
│   ├── config/               # 配置类
│   ├── dto/                  # 数据传输对象
│   ├── util/                 # 工具类
│   └── ShortUrlApplication.java # 启动类
├── src/main/resources/
│   ├── static/               # 静态资源
│   ├── templates/            # 模板文件
│   └── application.properties # 配置文件
└── pom.xml                   # Maven 配置
```

## 核心功能实现任务

### 第一阶段：基础框架搭建
- [ ] 创建 Spring Boot 项目
- [ ] 配置 pom.xml 依赖
- [ ] 配置 application.properties
- [ ] 创建项目基础目录结构

### 第二阶段：数据模型设计
- [ ] 设计 ShortUrl 实体类
- [ ] 设计 User 实体类
- [ ] 配置 JPA 实体关系
- [ ] 创建数据库表结构

### 第三阶段：核心服务实现
- [ ] 实现 ShortUrlService 服务类
- [ ] 实现短码生成算法（Base62编码）
- [ ] 实现短链接存储逻辑
- [ ] 实现短链接查询和跳转逻辑
- [ ] 实现访问统计功能

### 第四阶段：API接口开发
- [ ] 实现短链接生成接口 `/api/shortlinks/generate`
- [ ] 实现短链接跳转接口 `/api/shortlinks/{shortCode}`
- [ ] 实现短链接列表查询接口 `/api/shortlinks`
- [ ] 实现短链接CRUD接口
- [ ] 实现启用/禁用接口

### 第五阶段：安全认证
- [ ] 配置 Spring Security
- [ ] 实现用户认证服务
- [ ] 配置权限控制
- [ ] 实现登录接口

### 第六阶段：管理后台
- [ ] 实现管理员功能接口
- [ ] 实现数据统计接口
- [ ] 实现系统配置接口

## 依赖配置清单

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- MySQL Database Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Spring Boot DevTools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 数据库设计

### short_urls 表
```sql
CREATE TABLE short_urls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_url VARCHAR(1000) NOT NULL,
    short_code VARCHAR(255) UNIQUE NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    click_count BIGINT NOT NULL DEFAULT 0,
    app_id VARCHAR(255),
    updated_at DATETIME
);
```

### users 表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);
```

### user_roles 表
```sql
CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 开发规范

### 代码规范
- 使用 Lombok 简化 getter/setter
- 遵循 RESTful API 设计规范
- 统一异常处理机制
- 完善的日志记录

### 安全规范
- 密码加密存储（BCrypt）
- SQL 注入防护
- XSS 攻击防护
- CSRF 保护

### 测试规范
- 单元测试覆盖率 ≥ 80%
- 集成测试覆盖核心功能
- API 接口测试

## 部署要求

### 本地开发
- 端口：8080
- 数据库：localhost:3306
- 配置文件：application-dev.properties

### 生产环境
- 端口：8080
- 数据库连接池配置
- 日志配置
- 性能监控

## 验收标准

### 功能验收
- [ ] 短链接生成功能正常
- [ ] 302跳转功能正常
- [ ] 访问统计准确
- [ ] 管理后台功能完整
- [ ] 安全认证有效

### 性能验收
- [ ] QPS ≥ 1000
- [ ] 响应时间 ≤ 100ms
- [ ] 数据库连接稳定
- [ ] 内存使用合理

### 代码质量
- [ ] 代码注释完整
- [ ] 单元测试通过
- [ ] 无严重安全漏洞
- [ ] 符合编码规范

## 时间规划建议

| 阶段 | 预估时间 | 关键任务 |
|------|----------|----------|
| 环境搭建 | 2-3天 | 工具安装、项目初始化 |
| 核心功能 | 5-7天 | 短链接生成、跳转、统计 |
| 接口开发 | 3-4天 | RESTful API 实现 |
| 安全认证 | 2-3天 | 用户管理、权限控制 |
| 测试优化 | 2-3天 | 功能测试、性能优化 |
| 文档部署 | 1-2天 | README、部署文档 |

总计：15-20个工作日