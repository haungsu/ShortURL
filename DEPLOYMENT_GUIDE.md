# ShortURLPro 性能优化部署指南

## 🚀 部署概述

本指南详细介绍如何部署ShortURLPro系统的性能优化组件，包括Redis缓存、监控告警和日志分析系统。

## 📋 系统架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   应用服务   │    │   Redis缓存  │    │  MySQL数据库 │
│             │◄──►│             │◄──►│             │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Prometheus │    │   Grafana   │    │     ELK     │
│   监控收集   │    │  可视化面板  │    │  日志分析    │
└─────────────┘    └─────────────┘    └─────────────┘
```

## 🔧 部署步骤

### 1. 基础环境准备

确保系统已安装以下组件：
- Docker 20.10+
- Docker Compose 1.29+
- Java 21
- Maven 3.8+

### 2. 启动核心服务

```bash
# 启动Redis和MySQL
docker-compose up -d redis mysql

# 等待服务启动完成
docker-compose ps

# 启动应用服务
./mvnw spring-boot:run
```

### 3. 部署监控系统

```bash
# 进入监控目录
cd monitoring

# 启动完整的监控栈
docker-compose -f docker-compose.monitoring.yml up -d

# 检查服务状态
docker-compose -f docker-compose.monitoring.yml ps
```

### 4. 验证部署

#### 检查服务端口
```bash
# 应用服务
curl http://localhost:8080/actuator/health

# Prometheus
curl http://localhost:9090/-/healthy

# Grafana
curl http://localhost:3000/api/health

# Elasticsearch
curl http://localhost:9200/_cluster/health
```

#### 访问监控界面
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Kibana**: http://localhost:5601

## 📊 监控配置

### Prometheus配置
配置文件位置：`monitoring/prometheus/prometheus.yml`

主要监控目标：
- 应用服务指标 (`/actuator/prometheus`)
- 系统基础指标
- 自定义业务指标

### Grafana仪表板
导入预配置的仪表板：
1. 登录Grafana (http://localhost:3000)
2. 导入 `monitoring/grafana/dashboards/shorturl-dashboard.json`
3. 选择Prometheus数据源

### ELK日志分析
配置文件位置：
- Logstash管道：`monitoring/logstash/pipeline/`
- Logstash配置：`monitoring/logstash/config/`

## 🔧 性能调优

### Redis调优
```properties
# 连接池配置
spring.data.redis.lettuce.pool.max-active=20
spring.data.redis.lettuce.pool.max-idle=10
spring.data.redis.lettuce.pool.min-idle=2

# 超时配置
spring.data.redis.timeout=2000ms
```

### 数据库连接池调优
```properties
# HikariCP配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### JVM调优
```bash
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## 🚨 告警配置

### 告警规则示例
在Prometheus中配置告警规则：

```yaml
groups:
- name: shorturl-alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "高错误率警告"
      description: "5分钟内错误率超过5%"
```

### 告警通知
配置Alertmanager发送通知到：
- 邮件
- 钉钉/企业微信
- Slack
- 短信

## 📈 性能基准

### 预期性能指标
| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 响应时间 | 200ms | 100ms | 50% ↓ |
| QPS | 1000 | 3000 | 200% ↑ |
| 缓存命中率 | 60% | 90% | 50% ↑ |
| 可用性 | 99.5% | 99.9% | 0.4% ↑ |

## 🔧 故障排除

### 常见问题

1. **Redis连接失败**
   ```bash
   # 检查Redis服务状态
   docker logs short-url-redis
   
   # 测试连接
   redis-cli ping
   ```

2. **监控指标不显示**
   ```bash
   # 检查Actuator端点
   curl http://localhost:8080/actuator/prometheus
   
   # 检查Prometheus抓取状态
   curl http://localhost:9090/targets
   ```

3. **Grafana面板空白**
   - 确认数据源配置正确
   - 检查时间范围设置
   - 验证Prometheus查询语句

### 日志查看
```bash
# 查看应用日志
tail -f logs/application.log

# 查看容器日志
docker-compose logs -f [service_name]

# 查看监控组件日志
docker-compose -f monitoring/docker-compose.monitoring.yml logs -f
```

## 📝 维护计划

### 日常维护
- 每日检查系统健康状态
- 监控关键性能指标
- 定期清理过期日志

### 定期维护
- 每周备份重要数据
- 每月审查和优化配置
- 每季度进行压力测试

### 应急预案
- 制定服务降级方案
- 准备快速回滚计划
- 建立故障响应流程

## 🆘 技术支持

遇到问题时，请提供以下信息：
1. 错误日志
2. 系统配置
3. 复现步骤
4. 环境信息

联系邮箱：support@example.com