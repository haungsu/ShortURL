# ShortURLPro Docker 部署指南

## 📋 目录
1. [系统要求](#系统要求)
2. [快速部署](#快速部署)
3. [详细部署步骤](#详细部署步骤)
4. [配置说明](#配置说明)
5. [环境变量](#环境变量)
6. [监控部署](#监控部署)
7. [常见问题](#常见问题)
8. [维护操作](#维护操作)

## 系统要求

### 最低配置
- **CPU**: 2核
- **内存**: 4GB RAM
- **存储**: 20GB可用空间
- **操作系统**: Linux/Windows/macOS

### 推荐配置
- **CPU**: 4核或以上
- **内存**: 8GB RAM或以上
- **存储**: 50GB SSD
- **网络**: 稳定的互联网连接

### 软件依赖
- Docker 20.10+
- Docker Compose 1.29+
- Git (可选，用于获取代码)

## 快速部署

### 一键部署脚本

**Windows:**
```cmd
# 下载并运行部署脚本
curl -O https://raw.githubusercontent.com/your-repo/shorturl-pro/main/deploy-all.bat
deploy-all.bat
```

**Linux/macOS:**
```bash
# 下载并运行部署脚本
wget https://raw.githubusercontent.com/your-repo/shorturl-pro/main/deploy-all.sh
chmod +x deploy-all.sh
./deploy-all.sh
```

### 手动快速部署

```bash
# 克隆项目
git clone https://github.com/your-repo/shorturl-pro.git
cd shorturl-pro

# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 详细部署步骤

### 1. 环境准备

#### 安装 Docker
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install docker.io docker-compose -y

# CentOS/RHEL
sudo yum install docker docker-compose -y

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker
```

#### 验证安装
```bash
docker --version
docker-compose --version
```

### 2. 获取项目代码

```bash
# 方法1: Git克隆
git clone https://github.com/your-repo/shorturl-pro.git
cd shorturl-pro

# 方法2: 下载压缩包
wget https://github.com/your-repo/shorturl-pro/archive/main.zip
unzip main.zip
cd shorturl-pro-main
```

### 3. 配置环境变量

创建 `.env` 文件：
```bash
cp .env.example .env
```

编辑 `.env` 文件：
```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=short_url_db

# 应用配置
SHORT_URL_BASE_URL=http://your-domain.com
ADMIN_PASSWORD=your_admin_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# JVM配置
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC

# 时区配置
TZ=Asia/Shanghai
```

### 4. 启动服务

#### 启动基础服务
```bash
# 启动Redis和MySQL
docker-compose up -d redis mysql

# 等待服务初始化完成 (约30秒)
sleep 30

# 检查服务状态
docker-compose ps
```

#### 启动应用服务
```bash
# 构建应用镜像
docker build -t shorturl-pro:latest .

# 启动应用服务
docker-compose up -d short-url-app

# 或者直接使用预构建镜像
docker-compose up -d
```

### 5. 验证部署

```bash
# 检查服务状态
docker-compose ps

# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 检查数据库连接
docker-compose exec mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "SHOW DATABASES;"

# 检查Redis连接
docker-compose exec redis redis-cli ping
```

## 配置说明

### Docker Compose 配置详解

#### Redis 服务配置
```yaml
redis:
  image: redis:7-alpine
  container_name: short-url-redis
  restart: always
  ports:
    - "6379:6379"
  volumes:
    - short-url-redis-data:/data
  command: redis-server --appendonly yes
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 5s
    timeout: 10s
    retries: 10
```

#### MySQL 服务配置
```yaml
mysql:
  image: mysql:8.0
  container_name: short-url-mysql
  restart: always
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    MYSQL_DATABASE: ${MYSQL_DATABASE}
    MYSQL_CHARACTER_SET_SERVER: utf8mb4
    MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    TZ: Asia/Shanghai
  ports:
    - "3306:3306"
  volumes:
    - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    - short-url-mysql-data:/var/lib/mysql
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
```

#### 应用服务配置
```yaml
short-url-app:
  image: lattt/short-url-pro:0.0.1  # 或使用本地构建的镜像
  container_name: short-url-pro
  restart: always
  ports:
    - "8080:8080"
  environment:
    SPRING_PROFILES_ACTIVE: prod
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    JAVA_OPTS: ${JAVA_OPTS}
    SHORT_URL_BASE_URL: ${SHORT_URL_BASE_URL}
    TZ: Asia/Shanghai
  depends_on:
    mysql:
      condition: service_healthy
    redis:
      condition: service_healthy
  volumes:
    - short-url-app-logs:/app/logs
```

## 环境变量

### 核心环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `123456` | MySQL root密码 |
| `MYSQL_DATABASE` | `short_url_db` | 数据库名称 |
| `REDIS_PASSWORD` | `` | Redis密码(可选) |
| `SHORT_URL_BASE_URL` | `http://localhost:8080` | 短链接基础URL |
| `ADMIN_PASSWORD` | `admin123` | 管理员密码 |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | JVM参数 |
| `TZ` | `Asia/Shanghai` | 时区设置 |

### 生产环境推荐配置

```env
# 数据库安全配置
MYSQL_ROOT_PASSWORD=StrongPassword123!
MYSQL_DATABASE=shorturl_production

# 应用配置
SHORT_URL_BASE_URL=https://yourdomain.com
ADMIN_PASSWORD=SecureAdminPass456!

# Redis安全配置
REDIS_PASSWORD=RedisSecurePass789!

# JVM性能配置
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8

# 时区
TZ=Asia/Shanghai
```

## 监控部署

### 启动监控系统

```bash
# 进入监控目录
cd monitoring

# 启动完整监控栈
docker-compose -f docker-compose.monitoring.yml up -d

# 返回项目根目录
cd ..
```

### 监控组件说明

#### Prometheus (端口: 9090)
- 监控指标收集器
- 访问地址: http://localhost:9090
- 配置文件: `monitoring/prometheus/prometheus.yml`

#### Grafana (端口: 3000)
- 数据可视化面板
- 访问地址: http://localhost:3000
- 默认账号: admin/admin123
- 预配置仪表板: `monitoring/grafana/dashboards/`

#### ELK Stack
- **Elasticsearch** (端口: 9200) - 日志搜索引擎
- **Logstash** (端口: 5044) - 日志收集处理
- **Kibana** (端口: 5601) - 日志分析界面

### 导入Grafana仪表板

1. 登录Grafana (http://localhost:3000)
2. 导航到: `+` → `Import`
3. 上传文件: `monitoring/grafana/dashboards/shorturl-dashboard.json`
4. 选择Prometheus数据源
5. 点击"Import"

## 常见问题

### 1. 服务启动失败

**问题**: 容器无法正常启动
```bash
# 查看详细错误信息
docker-compose logs [service_name]

# 检查容器状态
docker-compose ps

# 重新构建镜像
docker-compose build --no-cache
```

### 2. 数据库连接失败

**问题**: 应用无法连接MySQL
```bash
# 检查MySQL服务状态
docker-compose logs mysql

# 测试数据库连接
docker-compose exec mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "SELECT 1;"

# 检查网络连通性
docker-compose exec short-url-app ping mysql
```

### 3. Redis连接超时

**问题**: Redis连接不稳定
```bash
# 检查Redis服务
docker-compose logs redis

# 测试Redis连接
docker-compose exec redis redis-cli ping

# 调整Redis配置
# 在docker-compose.yml中增加:
# command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
```

### 4. 内存不足

**问题**: 系统内存不足导致OOM
```bash
# 调整JVM内存设置
JAVA_OPTS=-Xms512m -Xmx1g

# 限制容器内存
# 在docker-compose.yml中为每个服务添加:
# mem_limit: 1g
# mem_reservation: 512m
```

### 5. 端口冲突

**问题**: 端口被其他程序占用
```bash
# 检查端口占用
netstat -tlnp | grep :8080

# 修改端口映射
# 在docker-compose.yml中修改ports部分:
# ports:
#   - "8081:8080"  # 将主机端口改为8081
```

## 维护操作

### 日常维护命令

```bash
# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f short-url-app

# 重启服务
docker-compose restart [service_name]

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v

# 更新应用
docker-compose pull
docker-compose up -d
```

### 数据备份

```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE > backup-$(date +%Y%m%d).sql

# 备份Redis数据
docker-compose exec redis redis-cli BGSAVE
docker cp short-url-redis:/data/dump.rdb ./redis-backup-$(date +%Y%m%d).rdb

# 备份应用日志
docker cp short-url-pro:/app/logs ./logs-backup-$(date +%Y%m%d)
```

### 性能监控

```bash
# 查看容器资源使用情况
docker stats

# 查看应用内存使用
docker-compose exec short-url-app jps -v

# 查看数据库性能
docker-compose exec mysql mysqladmin -uroot -p$MYSQL_ROOT_PASSWORD processlist

# 查看Redis性能
docker-compose exec redis redis-cli info
```

### 版本升级

```bash
# 1. 停止当前服务
docker-compose down

# 2. 备份数据
# 执行上面的备份命令

# 3. 拉取最新代码
git pull origin main

# 4. 重新构建镜像
docker-compose build

# 5. 启动服务
docker-compose up -d

# 6. 验证升级
curl http://localhost:8080/actuator/health
```

## 安全建议

### 1. 修改默认密码
```bash
# 修改.env文件中的密码
MYSQL_ROOT_PASSWORD=YourSecurePassword123!
ADMIN_PASSWORD=YourAdminPassword456!
REDIS_PASSWORD=YourRedisPassword789!
```

### 2. 启用HTTPS
```yaml
# 使用Nginx反向代理启用SSL
nginx:
  image: nginx:alpine
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./nginx.conf:/etc/nginx/nginx.conf
    - ./ssl:/etc/nginx/ssl
```

### 3. 限制访问
```bash
# 使用防火墙限制端口访问
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
ufw deny 3306   # MySQL (仅内网访问)
ufw deny 6379   # Redis (仅内网访问)
ufw enable
```

### 4. 定期更新
```bash
# 更新Docker镜像
docker-compose pull

# 更新系统包
apt update && apt upgrade -y

# 重启服务应用更新
docker-compose up -d
```

## 故障排除

### 应用启动异常
```bash
# 查看启动日志
docker-compose logs short-url-app

# 检查环境变量
docker-compose exec short-url-app env

# 进入容器调试
docker-compose exec short-url-app sh
```

### 数据库迁移失败
```bash
# 检查数据库版本
docker-compose exec mysql mysql -V

# 手动执行迁移
docker-compose exec short-url-app java -jar app.jar --spring.jpa.hibernate.ddl-auto=update
```

### 缓存问题
```bash
# 清除Redis缓存
docker-compose exec redis redis-cli FLUSHALL

# 重启Redis服务
docker-compose restart redis
```

---

## 📞 技术支持

如遇问题，请提供以下信息：
1. 错误日志输出
2. 系统环境信息
3. 复现步骤
4. 配置文件内容

**联系方式**: support@example.com

**GitHub Issues**: https://github.com/your-repo/shorturl-pro/issues