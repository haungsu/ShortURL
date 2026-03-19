# ShortURLPro 启动指南

## 快速启动

### Windows用户
双击运行以下任一脚本：

1. **`ensure-startup.bat`** (推荐) - 智能环境检查和启动
2. **`start-all.bat`** - 快捷启动菜单

### Linux/macOS用户
```bash
chmod +x ensure-startup.sh
./ensure-startup.sh
```

## 启动选项说明

### 1. 环境检查与智能启动 (推荐)
- 自动检测Java、Node.js、Maven等环境
- 检查并安装前端依赖
- 提供多种启动模式选择

### 2. 完整开发环境
需要Docker，自动启动：
- MySQL数据库 (端口3306)
- Redis缓存 (端口6379)
- Spring Boot后端 (端口8080)
- Vue前端开发服务器 (端口5173)

### 3. 基础开发环境
需要手动安装MySQL和Redis，启动：
- Spring Boot后端 (端口8080)
- Vue前端开发服务器 (端口5173)

## 访问地址

- **前端界面**: http://localhost:5173
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

## 环境要求

### 必需环境
- **Java**: JDK 21或更高版本
- **Node.js**: 18或更高版本
- **Maven**: 3.8或使用项目自带的Maven Wrapper

### 可选环境
- **Docker**: 用于一键启动数据库和缓存服务
- **MySQL**: 8.0 (如不使用Docker)
- **Redis**: 7.0 (如不使用Docker)

## 手动启动方式

### 启动后端
```bash
# Windows
mvnw spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

### 启动前端
```bash
cd ShortURLPro/vue
npm install  # 首次运行需要安装依赖
npm run dev
```

## 常见问题

### 1. 端口被占用
- 后端默认端口: 8080
- 前端默认端口: 5173
- 如需修改，在相应配置文件中调整

### 2. 数据库连接失败
确保：
- MySQL服务正在运行
- 数据库名: `short_url_db`
- 用户名: `root`
- 密码: `123456`

### 3. 前端依赖安装失败
```bash
cd ShortURLPro/vue
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### 4. 后端启动缓慢
首次启动会下载依赖，需要耐心等待。

## 开发建议

1. **推荐使用完整开发环境** (选项1)，最省心
2. **开发时使用热重载**，前后端都会自动重启
3. **查看日志**，两个服务都有详细的启动日志
4. **API测试**，使用Swagger UI进行接口测试

## 停止服务

### 使用脚本停止
```bash
# 如果使用Docker
docker compose down

# 手动关闭终端窗口即可
```

### 手动停止
- 关闭对应的终端窗口
- 或使用 Ctrl+C 终止进程