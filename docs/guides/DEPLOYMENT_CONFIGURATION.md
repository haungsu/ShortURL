# 部署配置指南

## 概述

本文档详细介绍如何配置ShortURLPro项目以适应不同的部署环境，包括本地开发、服务器部署、容器化部署等场景。

## 短链接域名配置

### 1. 配置项说明

项目支持多种方式配置短链接的基础域名：

#### 后端配置 (`application.yml`)
```yaml
app:
  short-url:
    base-url: ${SHORT_URL_BASE_URL:http://localhost:8080}
    code-length: 6
    default-expire-days: 30
```

#### 环境变量配置
```bash
# Linux/macOS
export SHORT_URL_BASE_URL=https://your-domain.com

# Windows (PowerShell)
$env:SHORT_URL_BASE_URL="https://your-domain.com"

# Windows (CMD)
set SHORT_URL_BASE_URL=https://your-domain.com
```

### 2. 不同部署场景的配置

#### 2.1 本地开发环境
```yaml
# application-dev.yml
app:
  short-url:
    base-url: http://localhost:8080
```

#### 2.2 生产服务器部署
```yaml
# application-prod.yml
app:
  short-url:
    base-url: https://your-production-domain.com
```

或者通过环境变量：
```bash
export SHORT_URL_BASE_URL=https://your-production-domain.com
```

#### 2.3 Docker部署
在 `docker-compose.yml` 中配置：
```yaml
environment:
  - SHORT_URL_BASE_URL=https://your-domain.com
```

#### 2.4 反向代理/Nginx部署
当使用Nginx等反向代理时，系统会自动识别 `X-Forwarded-*` 头部信息：

```nginx
location / {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host $host;
}
```

#### 2.5 内网穿透部署 (ngrok/FRP)
系统自动支持内网穿透工具的动态域名解析，无需额外配置。

### 3. 域名解析优先级

系统按照以下优先级确定短链接域名：

1. **反向代理头信息** (最高优先级)
   - `X-Forwarded-Host` + `X-Forwarded-Proto`
   - 适用于Nginx、Apache等反向代理

2. **原生请求信息**
   - `request.getScheme()` + `request.getServerName()` + `request.getServerPort()`
   - 适用于直接访问

3. **配置文件兜底**
   - `app.short-url.base-url` 配置项
   - 当无请求上下文时使用

### 4. 前端配置

#### 开发环境 (`.env.development`)
```env
VITE_API_BASE_URL=http://localhost:8080
```

#### 生产环境 (`.env.production`)
```env
# 推荐留空，使用相对路径
VITE_API_BASE_URL=

# 或指定具体域名
# VITE_API_BASE_URL=https://your-domain.com
```

### 5. 部署示例

#### 5.1 宝塔面板部署
```bash
# 1. 设置环境变量
echo 'export SHORT_URL_BASE_URL=https://your-domain.com' >> ~/.bashrc
source ~/.bashrc

# 2. 启动应用
java -jar ShortURLPro-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### 5.2 Docker部署
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/ShortURLPro-0.0.1-SNAPSHOT.jar app.jar
ENV SHORT_URL_BASE_URL=https://your-domain.com
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### 5.3 Kubernetes部署
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: shorturl-pro
        env:
        - name: SHORT_URL_BASE_URL
          value: "https://your-domain.com"
```

### 6. 验证配置

启动应用后，可以通过以下方式验证配置是否生效：

1. **查看日志**
```bash
# 查看启动日志中的域名信息
tail -f logs/application.log | grep "动态域名"
```

2. **API测试**
```bash
# 生成短链接测试
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com"}'
```

3. **浏览器访问**
访问管理页面，检查生成的短链接是否包含正确的域名。

### 7. 常见问题

#### Q: 短链接显示localhost而不是我的域名？
A: 检查以下几点：
- 确认设置了正确的 `SHORT_URL_BASE_URL` 环境变量
- 检查反向代理是否正确传递了 `X-Forwarded-*` 头部
- 查看应用日志确认域名解析过程

#### Q: 使用HTTPS时短链接还是HTTP？
A: 确保：
- 配置了正确的基础URL（包含https://）
- 反向代理正确设置了 `X-Forwarded-Proto: https`
- SSL证书配置正确

#### Q: 容器化部署时域名配置不生效？
A: 确认：
- 环境变量已正确传递到容器
- 容器网络配置允许访问外部域名
- DNS解析正常工作

### 8. 最佳实践

1. **生产环境始终使用HTTPS**
2. **通过环境变量而非配置文件管理域名**
3. **合理配置反向代理头信息传递**
4. **定期验证短链接域名配置的有效性**
5. **备份重要配置以防意外变更**

这样配置后，您的短链接系统就能灵活适应各种部署环境，不再受硬编码地址的限制。