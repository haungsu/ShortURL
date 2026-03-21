# ShortURLPro Nginx反向代理配置

## 📖 概述

本目录包含了ShortURLPro系统的完整Nginx反向代理配置，支持HTTP/HTTPS、负载均衡、SSL加密、性能优化等功能。

## 📁 目录结构

```
nginx/
├── nginx.conf              # Nginx主配置文件
├── conf.d/                 # 站点配置目录
│   ├── shorturl.conf      # 生产环境配置（HTTPS）
│   └── shorturl-dev.conf  # 开发环境配置（HTTP）
├── ssl/                   # SSL证书目录
│   ├── fullchain.pem     # 证书链文件
│   └── privkey.pem       # 私钥文件
├── logs/                  # 日志目录
├── generate-ssl.sh        # SSL证书生成脚本（Linux/Mac）
├── generate-ssl.bat       # SSL证书生成脚本（Windows）
└── README.md             # 本文件
```

## 🚀 快速开始

### Windows环境

```cmd
# 1. 双击运行部署脚本
deploy-nginx.bat

# 2. 或者手动部署
# 生成SSL证书
nginx\generate-ssl.bat shorturl.local

# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d
```

### Linux/Mac环境

```bash
# 1. 生成SSL证书
chmod +x nginx/generate-ssl.sh
nginx/generate-ssl.sh your-domain.com your-email@example.com

# 2. 启动服务
docker-compose -f docker-compose.nginx.yml up -d
```

## 🛠️ 主要特性

### 🔐 安全特性
- **SSL/TLS加密**: 支持HTTPS访问
- **安全头设置**: HSTS、X-Frame-Options等
- **访问控制**: IP白名单、基本认证
- **防攻击配置**: 限制请求频率、隐藏版本信息

### ⚡ 性能优化
- **Gzip压缩**: 减少传输数据量
- **缓存策略**: 静态资源长期缓存
- **连接优化**: Keep-alive、连接池
- **负载均衡**: 多实例分担负载

### 📊 监控功能
- **健康检查**: 自动检测服务状态
- **详细日志**: 记录访问和错误信息
- **性能指标**: 响应时间、连接数统计

## 📋 配置文件说明

### nginx.conf (主配置)
- 全局性能优化设置
- 工作进程配置
- 日志格式定义
- Gzip压缩配置

### shorturl.conf (生产环境)
- HTTPS强制重定向
- SSL证书配置
- 完整安全头设置
- 负载均衡配置

### shorturl-dev.conf (开发环境)
- HTTP-only配置
- 宽松安全设置
- 便于调试的日志
- 简化代理规则

## 🔧 环境变量

在 `.env` 文件中配置：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=short_url_db

# Redis配置
REDIS_PASSWORD=your_redis_password

# 应用配置
SHORT_URL_BASE_URL=https://your-domain.com
ADMIN_PASSWORD=your_admin_password

# JVM配置
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
```

## 🌐 访问地址

### 开发环境
- **HTTP**: http://localhost
- **应用**: http://localhost/api/
- **健康检查**: http://localhost/health

### 生产环境
- **HTTPS**: https://your-domain.com
- **应用**: https://your-domain.com/api/
- **健康检查**: https://your-domain.com/health
- **监控**: https://your-domain.com/metrics

## 📊 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx HTTP | 80 | HTTP访问入口 |
| Nginx HTTPS | 443 | HTTPS访问入口 |
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| Prometheus | 9090 | 监控服务 |
| Grafana | 3000 | 可视化面板 |

## 🔍 故障排除

### 常见问题

1. **502 Bad Gateway**
   ```bash
   docker-compose logs nginx
   docker-compose ps short-url-app
   ```

2. **SSL证书错误**
   ```bash
   openssl x509 -in nginx/ssl/fullchain.pem -text -noout
   ```

3. **连接超时**
   ```bash
   # 检查防火墙设置
   netstat -tlnp | grep :80
   ```

### 调试命令

```bash
# 测试Nginx配置
docker-compose exec nginx nginx -t

# 重载Nginx配置
docker-compose exec nginx nginx -s reload

# 查看实时日志
docker-compose logs -f nginx

# 检查服务状态
docker-compose ps
```

## 🛡️ 安全建议

### 生产环境必做事项
1. 使用有效的SSL证书（Let's Encrypt）
2. 配置防火墙规则
3. 定期更新Docker镜像
4. 启用日志轮转
5. 设置监控告警

### 安全配置检查清单
- [ ] SSL证书有效且未过期
- [ ] 安全头正确配置
- [ ] 访问控制策略实施
- [ ] 日志记录完整
- [ ] 定期安全扫描

## 📈 性能调优

### 关键配置参数

```nginx
# 连接优化
keepalive_timeout 65;
keepalive_requests 100;

# 缓冲区设置
client_body_buffer_size 128k;
client_max_body_size 10m;

# Gzip压缩
gzip_comp_level 6;
gzip_min_length 1024;
```

### 监控指标关注点
- 请求响应时间
- 并发连接数
- 错误率统计
- 资源使用情况

## 🔄 维护操作

### 日常维护
```bash
# 查看服务状态
docker-compose ps

# 查看资源使用
docker stats

# 清理日志
> nginx/logs/access.log
```

### 版本升级
```bash
# 备份配置
cp -r nginx nginx.backup.$(date +%Y%m%d)

# 更新镜像
docker-compose pull

# 重启服务
docker-compose up -d
```

## 📚 参考文档

- [Nginx官方文档](http://nginx.org/en/docs/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Let's Encrypt文档](https://letsencrypt.org/docs/)
- [SSL Labs测试](https://www.ssllabs.com/ssltest/)

## 🆘 技术支持

如遇问题，请提供以下信息：
1. 错误日志内容
2. 系统环境信息
3. 配置文件内容
4. 复现步骤

**GitHub Issues**: https://github.com/your-repo/shorturl-pro/issues

---

**注意**: 请根据实际环境调整配置参数，特别是域名、证书路径和安全设置。