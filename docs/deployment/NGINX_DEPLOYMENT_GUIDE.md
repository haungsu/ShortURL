# Nginx反向代理部署指南

## 📋 目录
1. [概述](#概述)
2. [目录结构](#目录结构)
3. [快速开始](#快速开始)
4. [详细配置说明](#详细配置说明)
5. [SSL证书配置](#ssl证书配置)
6. [Docker部署](#docker部署)
7. [性能优化](#性能优化)
8. [监控和日志](#监控和日志)
9. [故障排除](#故障排除)
10. [安全建议](#安全建议)

## 概述

本配置为ShortURLPro系统提供了完整的Nginx反向代理解决方案，包含：
- HTTP/HTTPS反向代理
- 负载均衡配置
- SSL/TLS加密支持
- 性能优化设置
- 安全头配置
- 健康检查机制

## 目录结构

```
nginx/
├── nginx.conf              # 主配置文件
├── conf.d/
│   ├── shorturl.conf      # 生产环境配置（HTTPS）
│   └── shorturl-dev.conf  # 开发环境配置（HTTP）
├── ssl/                   # SSL证书目录
│   ├── fullchain.pem     # 证书链文件
│   └── privkey.pem       # 私钥文件
├── logs/                  # 日志目录
├── generate-ssl.sh        # SSL证书生成脚本（Linux/Mac）
└── generate-ssl.bat       # SSL证书生成脚本（Windows）
```

## 快速开始

### 1. 开发环境部署

```bash
# 1. 生成自签名SSL证书（Windows）
cd nginx
generate-ssl.bat shorturl.local

# 2. 启动开发环境
cd ..
docker-compose -f docker-compose.dev.yml up -d

# 3. 访问应用
# http://localhost
```

### 2. 生产环境部署

```bash
# 1. 获取SSL证书
cd nginx
./generate-ssl.sh your-domain.com your-email@example.com

# 2. 启动完整环境
cd ..
docker-compose -f docker-compose.nginx.yml up -d

# 3. 访问应用
# https://your-domain.com
```

## 详细配置说明

### 主配置文件 (nginx.conf)

包含全局设置：
- 工作进程优化
- 性能调优参数
- Gzip压缩配置
- 日志格式定义
- 安全缓冲区设置

### 站点配置

#### 生产环境配置 (shorturl.conf)
- 强制HTTPS重定向
- SSL/TLS安全配置
- 完整的安全头设置
- 针对不同路径的优化策略
- 健康检查端点

#### 开发环境配置 (shorturl-dev.conf)
- HTTP-only配置
- 宽松的安全设置
- 便于调试的日志配置
- 简化的代理规则

### 负载均衡配置

```nginx
# 主应用负载均衡
upstream shorturl_backend {
    least_conn;  # 最少连接算法
    server short-url-app:8080 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

# 短链接专用负载均衡
upstream shorturl_redirect {
    ip_hash;  # IP哈希保持会话
    server short-url-app:8080 max_fails=2 fail_timeout=10s;
    keepalive 64;
}
```

## SSL证书配置

### 自动生成证书

#### Windows环境
```cmd
nginx\generate-ssl.bat your-domain.com
```

#### Linux/Mac环境
```bash
chmod +x nginx/generate-ssl.sh
nginx/generate-ssl.sh your-domain.com your-email@example.com
```

### Let's Encrypt证书

```bash
# 安装certbot
sudo apt install certbot  # Ubuntu/Debian
sudo yum install certbot  # CentOS/RHEL

# 获取证书
sudo certbot certonly --standalone -d your-domain.com
```

### 证书续期

```bash
# 手动续期
sudo certbot renew

# 自动续期（添加到crontab）
0 12 * * * /usr/bin/certbot renew --quiet
```

## Docker部署

### 环境变量配置

创建 `.env` 文件：
```env
MYSQL_ROOT_PASSWORD=your_secure_password
REDIS_PASSWORD=your_redis_password
SHORT_URL_BASE_URL=https://your-domain.com
```

### 启动命令

```bash
# 开发环境
docker-compose -f docker-compose.dev.yml up -d

# 生产环境
docker-compose -f docker-compose.nginx.yml up -d

# 查看状态
docker-compose -f docker-compose.nginx.yml ps

# 查看日志
docker-compose -f docker-compose.nginx.yml logs -f nginx
```

### 服务端口映射

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx HTTP | 80 | HTTP访问入口 |
| Nginx HTTPS | 443 | HTTPS访问入口 |
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| Prometheus | 9090 | 监控服务 |
| Grafana | 3000 | 可视化面板 |

## 性能优化

### Nginx优化配置

```nginx
# 连接优化
keepalive_timeout 65;
keepalive_requests 100;

# 缓冲区优化
client_body_buffer_size 128k;
client_max_body_size 10m;

# Gzip压缩
gzip on;
gzip_comp_level 6;
gzip_types text/plain application/json;
```

### 应用层优化

```nginx
# 短链接跳转优化
location ~ ^/[a-zA-Z0-9]{6,10}$ {
    proxy_buffering off;  # 减少延迟
    proxy_cache_valid 200 1h;  # 缓存成功响应
    proxy_next_upstream_tries 3;  # 故障转移
}
```

### 静态资源优化

```nginx
# 静态资源长期缓存
location ~* \.(jpg|jpeg|png|gif|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

## 监控和日志

### 日志文件位置

```
nginx/logs/
├── access.log           # 访问日志
├── error.log            # 错误日志
├── shorturl.access.log  # 站点访问日志
└── shorturl.error.log   # 站点错误日志
```

### 自定义日志格式

```nginx
log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                '$status $body_bytes_sent "$http_referer" '
                '"$http_user_agent" rt=$request_time';
```

### 健康检查

```nginx
# 内置健康检查端点
location /health {
    access_log off;
    proxy_pass http://shorturl_backend/actuator/health;
}
```

### 监控指标

通过Prometheus监控：
- 请求速率和响应时间
- 错误率统计
- 连接数和资源使用
- 自定义业务指标

## 故障排除

### 常见问题

#### 1. 502 Bad Gateway
```bash
# 检查上游服务状态
docker-compose ps short-url-app

# 查看Nginx错误日志
docker-compose logs nginx

# 测试上游连接
docker-compose exec nginx wget http://short-url-app:8080
```

#### 2. SSL证书问题
```bash
# 验证证书有效性
openssl x509 -in nginx/ssl/fullchain.pem -text -noout

# 检查证书权限
ls -la nginx/ssl/

# 重新生成证书
nginx/generate-ssl.sh your-domain.com
```

#### 3. 连接超时
```nginx
# 调整超时设置
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
```

#### 4. 负载过高
```bash
# 查看Nginx状态
docker stats short-url-nginx

# 检查连接数
ss -tuln | grep :80

# 调整工作进程
worker_processes auto;  # 根据CPU核心数自动调整
```

### 调试技巧

```bash
# 启用调试日志
docker-compose exec nginx nginx -t  # 测试配置
docker-compose exec nginx nginx -s reload  # 重载配置

# 实时监控日志
tail -f nginx/logs/access.log
tail -f nginx/logs/error.log

# 测试配置语法
docker-compose exec nginx nginx -t
```

## 安全建议

### 1. 基础安全配置

```nginx
# 隐藏版本信息
server_tokens off;

# 限制请求方法
if ($request_method !~ ^(GET|POST|PUT|DELETE|OPTIONS)$ ) {
    return 405;
}

# 限制请求频率
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
```

### 2. SSL安全强化

```nginx
# 强制TLS 1.2+
ssl_protocols TLSv1.2 TLSv1.3;

# 安全密码套件
ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;

# HSTS头
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

### 3. 访问控制

```nginx
# IP白名单
allow 192.168.1.0/24;
allow 10.0.0.0/8;
deny all;

# 管理接口保护
location /admin {
    auth_basic "Restricted Area";
    auth_basic_user_file /etc/nginx/.htpasswd;
}
```

### 4. 定期安全检查

```bash
# SSL证书检查
openssl s_client -connect your-domain.com:443

# 安全头验证
curl -I https://your-domain.com

# 配置测试
docker-compose exec nginx nginx -t
```

## 维护操作

### 日常维护

```bash
# 查看服务状态
docker-compose -f docker-compose.nginx.yml ps

# 查看资源使用
docker stats

# 清理日志文件
> nginx/logs/access.log
> nginx/logs/error.log

# 重启Nginx服务
docker-compose -f docker-compose.nginx.yml restart nginx
```

### 版本升级

```bash
# 1. 备份配置
cp -r nginx nginx.backup.$(date +%Y%m%d)

# 2. 拉取最新镜像
docker-compose -f docker-compose.nginx.yml pull

# 3. 重启服务
docker-compose -f docker-compose.nginx.yml up -d

# 4. 验证升级
curl -I https://your-domain.com
```

### 性能调优

根据实际负载调整：
- `worker_processes`: CPU核心数
- `worker_connections`: 并发连接数
- `keepalive`: 长连接数量
- 缓存策略和过期时间

---

**注意**: 请根据实际环境调整配置参数，特别是域名、证书路径和安全设置。