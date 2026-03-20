# ShortURLPro 项目启动说明

## 📋 项目概览

ShortURLPro 是一个基于 Spring Boot 3.3.5 + Vue 3 的短链接管理系统，提供完整的前后端分离架构。

## 🚀 启动方式总览

### 1. 一键启动（推荐）

```bash
# Windows
start-all.bat

# Linux/macOS  
./start-all.sh
```

### 2. 分步启动

#### 前端服务启动
```bash
cd vue
npm install
npm run dev
```
- **访问地址**: http://localhost:5173
- **默认端口**: 5173

#### 后端服务启动
```bash
# 方式一：使用Maven
./mvnw spring-boot:run

# 方式二：IDE启动
# 在IDE中运行 ShortUrlProApplication.java
```
- **访问地址**: http://localhost:8080
- **默认端口**: 8080

#### 基础设施服务启动
```bash
# 如果有docker-compose.yml文件
docker-compose up -d mysql redis
```

## 🛠️ 环境要求

### 必需环境
- **Java**: 21+
- **Node.js**: 20.19+ 或 22.12+
- **Maven**: 3.8+ (或使用mvnw wrapper)

### 可选环境
- **Docker & Docker Compose**: 用于启动MySQL和Redis
- **MySQL**: 8.0+ (可使用Docker)
- **Redis**: 7+ (可使用Docker)

## ⚙️ 配置说明

### 后端配置文件
- **主配置**: `src/main/resources/application.yml` (默认激活dev环境)
- **开发环境**: `src/main/resources/application-dev.yml`
- **生产环境**: `src/main/resources/application-prod.yml`

### 前端配置文件
- **开发环境**: `vue/.env.development`
- **生产环境**: `vue/.env.production`

### 核心配置项

**后端数据库配置**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/short_url_db
    username: root
    password: 123456
```

**后端Redis配置**:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

**前端API配置**:
```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 📊 服务端口汇总

| 服务 | 端口 | 访问地址 | 说明 |
|------|------|----------|------|
| 前端开发服务器 | 5173 | http://localhost:5173 | Vite开发服务器 |
| 后端应用服务 | 8080 | http://localhost:8080 | Spring Boot应用 |
| MySQL数据库 | 3306 | localhost:3306 | 数据存储 |
| Redis缓存 | 6379 | localhost:6379 | 缓存服务 |
| Prometheus监控 | 9090 | http://localhost:9090 | 指标收集 |
| Grafana面板 | 3000 | http://localhost:3000 | 数据可视化 |

## 🔧 启动脚本说明

### Windows启动脚本
- `start-all.bat`: 主启动菜单脚本
- 功能选项：
  1. 环境检查与智能启动
  2. 前端开发服务器启动
  3. 前端构建和预览
  4. 后端服务启动
  5. 前后端同时启动
  6. 完整开发环境启动

### Linux/macOS启动脚本
- `ensure-startup.sh`: 环境检查和启动脚本
- 自动检测环境依赖
- 跨平台兼容启动

## 🎯 开发环境启动流程

### 1. 环境准备
```bash
# 检查Java版本
java -version

# 检查Node.js版本
node --version

# 检查Maven版本
mvn --version
```

### 2. 依赖安装
```bash
# 前端依赖安装
cd vue
npm install

# 后端依赖由Maven自动管理
```

### 3. 服务启动顺序
1. **启动基础设施** (如果使用Docker):
   ```bash
   docker-compose up -d mysql redis
   ```

2. **启动后端服务**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **启动前端服务**:
   ```bash
   cd vue
   npm run dev
   ```

## 🔍 验证服务状态

### 后端健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 前端访问验证
浏览器访问: http://localhost:5173

### API文档访问
Swagger UI: http://localhost:8080/swagger-ui.html

## 🚨 常见问题解决

### 端口占用问题
```bash
# Windows查看端口占用
netstat -ano | findstr :8080

# Linux/macOS查看端口占用
lsof -i :8080
```

### 依赖安装失败
```bash
# 清理npm缓存
npm cache clean --force

# 重新安装依赖
rm -rf node_modules package-lock.json
npm install
```

### 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库配置信息
- 确认数据库用户权限

## 📈 生产环境部署

### Docker部署
```bash
# 构建镜像
docker build -t shorturl-pro:latest .

# 启动服务
docker-compose up -d
```

### 传统部署
```bash
# 构建生产包
./mvnw clean package

# 运行生产环境
java -jar -Dspring.profiles.active=prod target/ShortURLPro-0.0.1-SNAPSHOT.jar
```

## 🛡️ 安全配置

### 默认管理员账户
- **用户名**: admin
- **密码**: admin123

### 生产环境安全建议
- 修改默认管理员密码
- 配置HTTPS证书
- 设置防火墙规则
- 启用日志审计

## 📚 相关文档

- [README.md](README.md) - 项目介绍和基本使用
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - 部署指南
- [DOCKER_DEPLOYMENT_GUIDE.md](DOCKER_DEPLOYMENT_GUIDE.md) - Docker部署详细说明
- [TECH_STACK_DOCUMENTATION.md](TECH_STACK_DOCUMENTATION.md) - 技术栈详解

这份启动说明涵盖了ShortURLPro项目的各种启动方式和配置要点，您可以根据实际需求选择合适的启动方案。