# ShortURL Pro - 短链接管理系统

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-blue.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

🚀 一个高性能、易扩展的短链接管理系统，支持URL缩短、跳转追踪和数据分析

</div>

## 🌟 项目特色

- 🔗 **智能短链接生成** - 基于Base62编码的高效短码生成算法
- 📊 **实时访问统计** - 完整的点击量追踪和数据分析
- 🔐 **安全认证体系** - JWT Token + RBAC权限控制
- ⚡ **多级缓存优化** - Redis + Caffeine本地缓存提升性能
- 🎨 **现代化前端** - Vue 3 + TypeScript + TailwindCSS响应式界面
- 📈 **系统监控** - Prometheus + Grafana完整的监控告警体系
- 🐳 **容器化部署** - Docker Compose一键部署所有服务

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Vue 3 前端     │    │ Spring Boot 后端 │    │  MySQL 数据库    │
│                 │◄──►│                 │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
       │                       │                       │
       ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Redis 缓存     │    │ Prometheus监控   │    │   短链接实体     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 快速开始

### 环境要求

- Java 21+
- Node.js 20.19+ 或 22.12+
- Docker & Docker Compose (推荐)
- MySQL 8.0+ (可选，可用Docker)

### 一键启动 (推荐)

```bash
# Windows
start-all.bat

# Linux/Mac
./start-all.sh
```

### 分步启动

#### 1. 启动基础设施服务

```bash
# 启动MySQL和Redis
docker-compose up -d mysql redis

# 等待服务就绪
docker-compose ps
```

#### 2. 启动后端服务

```bash
# 方式一：使用Maven
./mvnw spring-boot:run

# 方式二：直接运行Jar包
./mvnw clean package
java -jar target/ShortURLPro-0.0.1-SNAPSHOT.jar
```

#### 3. 启动前端开发服务器

```bash
cd ShortURLPro/vue
npm install
npm run dev
```

### 访问地址

- 🌐 **前端界面**: http://localhost:5173
- 🚀 **API文档**: http://localhost:8080/swagger-ui.html
- 📊 **监控面板**: http://localhost:3000 (Grafana)
- 📈 **指标收集**: http://localhost:9090 (Prometheus)

## 🎯 核心功能

### 🔗 短链接管理

- 自动生成6位短码
- 支持自定义短链接
- 批量导入导出
- 状态管理（启用/禁用）
- 过期时间设置

### 📊 数据统计

- 实时点击量统计
- 访问来源分析
- 地理位置分布
- 时间趋势图表
- 导出Excel报表

### 👤 用户管理

- JWT Token认证
- 管理员角色控制
- 权限分级管理
- 登录状态保持

### ⚡ 性能优化

- 多级缓存机制
- 异步处理
- 连接池优化
- 数据库索引优化

## 🔧 API接口

### 公开接口

```bash
# 生成短链接
POST /api/short-url/generate
{
  "originalUrl": "https://www.example.com",
  "appId": "myapp"
}

# 短链接跳转
GET /{shortCode}
```

### 管理员接口

```bash
# 管理员登录
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# 获取短链接列表
GET /api/short-url/admin/list?page=0&size=10

# 创建短链接
POST /api/short-url
{
  "name": "我的链接",
  "originalUrl": "https://example.com",
  "status": "ENABLED"
}
```

详细的API文档请参考 [API接口文档](API_INTERFACE_DOCUMENT.md)

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.3.5
- **语言**: Java 21
- **数据库**: MySQL 8.0
- **缓存**: Redis 7 + Caffeine
- **安全**: Spring Security + JWT
- **ORM**: JPA/Hibernate
- **监控**: Micrometer + Prometheus
- **文档**: SpringDoc OpenAPI 3

### 前端技术
- **框架**: Vue 3.5
- **语言**: TypeScript
- **路由**: Vue Router 5
- **状态管理**: Pinia
- **样式**: TailwindCSS
- **构建工具**: Vite 7
- **测试**: Vitest + Playwright

### 运维技术
- **容器化**: Docker + Docker Compose
- **监控**: Prometheus + Grafana
- **日志**: ELK Stack (可选)
- **部署**: CI/CD ready

## 📁 项目结构

```
ShortURLPro/
├── src/main/java/com/example/shorturlpro/    # 后端源码
│   ├── controller/                           # 控制器层
│   ├── service/                              # 服务层
│   ├── entity/                               # 实体类
│   ├── repository/                           # 数据访问层
│   ├── config/                               # 配置类
│   └── util/                                 # 工具类
├── src/main/resources/                       # 资源文件
│   ├── application.properties                # 应用配置
│   └── static/                               # 静态资源
├── ShortURLPro/vue/                          # 前端项目
│   ├── src/                                  # Vue源码
│   │   ├── views/                            # 页面组件
│   │   ├── api/                              # API接口
│   │   ├── stores/                           # 状态管理
│   │   └── router/                           # 路由配置
│   └── package.json                          # 前端依赖
├── monitoring/                               # 监控配置
│   ├── prometheus/                           # Prometheus配置
│   ├── grafana/                              # Grafana仪表板
│   └── docker-compose.monitoring.yml         # 监控服务编排
├── docker-compose.yml                        # 主服务编排
├── pom.xml                                   # Maven配置
└── README.md                                 # 项目文档
```

## ⚙️ 配置说明

### 应用配置 (application.properties)

```properties
# 服务器配置
server.port=8080
app.short-url.base-url=http://localhost:8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/short_url_db
spring.datasource.username=root
spring.datasource.password=123456

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379

# 管理员账户
spring.security.user.name=admin
spring.security.user.password=admin123
```

### 环境变量配置

```bash
# 数据库连接
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/short_url_db
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=123456

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 管理员密码
export ADMIN_PASSWORD=admin123

# 短链接基础URL
export SHORT_URL_BASE_URL=http://your-domain.com
```

## 📊 监控与运维

### 启动监控系统

```bash
# 进入监控目录
cd monitoring

# 启动监控栈
docker-compose -f docker-compose.monitoring.yml up -d

# 验证服务状态
docker-compose -f docker-compose.monitoring.yml ps
```

### 性能指标

- **响应时间**: 平均 < 100ms
- **吞吐量**: 支持 3000+ QPS
- **缓存命中率**: > 90%
- **系统可用性**: 99.9%

### 告警配置

监控系统包含以下关键告警：
- 高错误率告警 (>5%)
- 响应时间异常告警 (>500ms)
- 系统资源使用率告警
- 数据库连接池耗尽告警

## 🔒 安全特性

### 认证安全
- ✅ JWT Token认证 (RS256算法)
- ✅ Token有效期管理 (2小时访问 + 7天刷新)
- ✅ 密码加密存储
- ✅ 登录失败次数限制

### 访问控制
- ✅ 基于角色的访问控制 (RBAC)
- ✅ 接口权限验证
- ✅ CORS跨域保护
- ✅ CSRF防护

### 数据安全
- ✅ SQL注入防护
- ✅ XSS攻击防护
- ✅ 输入参数校验
- ✅ 敏感信息脱敏

## 🧪 测试

### 运行单元测试

```bash
# 后端测试
./mvnw test

# 前端测试
cd ShortURLPro/vue
npm run test:unit
```

### API测试示例

```bash
# 管理员登录获取Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 生成短链接
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com"}'
```

## 🚀 部署指南

### 生产环境部署

详细的部署指南请参考 [部署文档](DEPLOYMENT_GUIDE.md)

### Docker部署

```bash
# 构建镜像
docker build -t short-url-pro:latest .

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### Kubernetes部署 (可选)

```bash
# 部署到K8s集群
kubectl apply -f k8s/

# 查看部署状态
kubectl get pods
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 开发流程

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范

- 后端遵循Google Java Style Guide
- 前端遵循ESLint + Prettier规范
- 提交信息遵循Conventional Commits规范

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 技术支持

- 💬 Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 📧 邮箱: support@example.com
- 📚 文档: [Wiki](https://github.com/your-repo/wiki)

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Redis](https://redis.io/)
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)

---

<p align="center">
  Made with ❤️ by ShortURL Pro Team
</p>