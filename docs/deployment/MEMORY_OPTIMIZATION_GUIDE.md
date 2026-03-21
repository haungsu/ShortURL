# ShortURLPro 内存优化指南

## 📊 内存优化配置总览

本文档提供 ShortURLPro 项目的内存优化配置建议，帮助降低内存占用并提升性能。

---

## 🎯 JVM 内存优化

### 1. 推荐配置（根据物理内存）

| 系统内存 | 初始堆 (-Xms) | 最大堆 (-Xmx) | 元空间初始 | 元空间最大 | 适用场景 |
|---------|-------------|-------------|----------|----------|---------|
| < 2GB   | 256m        | 512m        | 128m     | 256m     | 低配服务器/开发机 |
| 2-4GB   | 512m        | 1g          | 128m     | 256m     | 普通开发环境 |
| 4-8GB   | 1g          | 2g          | 256m     | 512m     | 生产环境（中等负载） |
| 8-16GB  | 2g          | 4g          | 512m     | 1g       | 生产环境（高负载） |
| > 16GB  | 4g          | 8g          | 1g       | 2g       | 高性能服务器 |

### 2. JVM 参数说明

```bash
# 基础内存配置
-Xms512m                    # 初始堆大小
-Xmx1g                      # 最大堆大小
-XX:MetaspaceSize=128m      # 元空间初始大小
-XX:MaxMetaspaceSize=256m   # 元空间最大大小

# GC 优化
-XX:+UseG1GC                # 使用 G1 垃圾回收器
-XX:MaxGCPauseMillis=200    # 最大 GC 停顿时间目标

# 内存泄漏诊断
-XX:+HeapDumpOnOutOfMemoryError  # OOM 时生成堆转储
-XX:HeapDumpPath=logs/heapdump.hprof  # 堆转储文件路径

# 其他优化
-XX:+DisableExplicitGC      # 禁用 System.gc() 调用
-XX:SoftRefLRUPolicyMSPerMB=1000  # 软引用保留策略
```

---

## 💾 数据库连接池优化 (HikariCP)

### 开发环境配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 2         # 最小空闲连接
      maximum-pool-size: 10   # 最大连接数
      idle-timeout: 300000    # 空闲超时 (5 分钟)
      max-lifetime: 600000    # 连接最大生命周期 (10 分钟)
      connection-timeout: 10000  # 连接超时 (10 秒)
      leak-detection-threshold: 30000  # 连接泄漏检测阈值 (30 秒)
```

### 生产环境配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5         # 最小空闲连接
      maximum-pool-size: 20   # 最大连接数
      idle-timeout: 600000    # 空闲超时 (10 分钟)
      max-lifetime: 1800000   # 连接最大生命周期 (30 分钟)
      connection-timeout: 20000  # 连接超时 (20 秒)
      leak-detection-threshold: 60000  # 连接泄漏检测阈值 (60 秒)
```

### 内存占用估算

每个数据库连接约占用 **1-2MB** 内存：
- 开发环境：10 连接 × 2MB = **20MB**
- 生产环境：20 连接 × 2MB = **40MB**

---

## 🔴 Redis 连接池优化 (Lettuce)

### 开发环境配置

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 8       # 最大活跃连接
          max-idle: 4         # 最大空闲连接
          min-idle: 1         # 最小空闲连接
          max-wait: 1000ms    # 最大等待时间
```

### 生产环境配置

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20      # 最大活跃连接
          max-idle: 10        # 最大空闲连接
          min-idle: 2         # 最小空闲连接
          max-wait: 2000ms    # 最大等待时间
```

### 内存占用估算

每个 Redis 连接约占用 **0.5-1MB** 内存：
- 开发环境：8 连接 × 1MB = **8MB**
- 生产环境：20 连接 × 1MB = **20MB**

---

## 🧵 线程池优化

### 通用异步任务线程池

```java
核心线程数：CPU 核心数
最大线程数：CPU 核心数 × 2
队列容量：100（有界队列）
空闲超时：60 秒
```

### 点击统计专用线程池

```java
核心线程数：2
最大线程数：5
队列容量：500
空闲超时：30 秒
```

### 内存占用估算

每个线程约占用 **1MB** 栈空间：
- 通用线程池：8 核心 × 2 × 1MB = **16MB**
- 点击统计池：5 × 1MB = **5MB**

---

## 🗄️ 缓存优化

### Caffeine 本地缓存

```yaml
# 建议配置
caffeine:
  spec: maximumSize=1000,expireAfterWrite=1h
```

内存占用：1000 条目 × 平均 1KB = **~1MB**

### Redis 缓存

```yaml
spring:
  cache:
    redis:
      time-to-live: 3600000  # 1 小时 TTL
      cache-null-values: false  # 不缓存空值
```

---

## 🐳 Docker 容器资源限制

### 开发环境

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 768M
    reservations:
      cpus: '0.5'
      memory: 512M
```

### 生产环境

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
    reservations:
      cpus: '1.0'
      memory: 1G
```

---

## 📈 监控与调优

### 1. 启用内存监控

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    enable:
      jvm.memory.used: true
      jvm.memory.committed: true
      jvm.memory.max: true
```

### 2. 关键指标

通过 Prometheus + Grafana 监控：
- `jvm_memory_used_bytes` - JVM 已使用内存
- `jvm_memory_committed_bytes` - JVM 已提交内存
- `hikaricp_connections_active` - 活跃数据库连接
- `hikaricp_connections_idle` - 空闲数据库连接
- `redis_lettuce_pool_active` - 活跃 Redis 连接

### 3. GC 日志分析

```bash
# 添加 JVM 参数
-Xlog:gc*:file=logs/gc.log:time,uptime:filecount=5,filesize=10M
```

---

## ⚡ 快速优化清单

### 开发环境（最低内存）

- [ ] JVM: `-Xms256m -Xmx512m`
- [ ] HikariCP: `maximum-pool-size=10`
- [ ] Redis Pool: `max-active=8`
- [ ] 线程池：核心线程数 = CPU 核心数
- [ ] 预计总内存：**~300-400MB**

### 生产环境（标准配置）

- [ ] JVM: `-Xms1g -Xmx2g`
- [ ] HikariCP: `maximum-pool-size=20`
- [ ] Redis Pool: `max-active=20`
- [ ] 线程池：核心线程数 × 2
- [ ] 预计总内存：**~1.5-2.5GB**

### 生产环境（高配）

- [ ] JVM: `-Xms2g -Xmx4g`
- [ ] HikariCP: `maximum-pool-size=30`
- [ ] Redis Pool: `max-active=30`
- [ ] 线程池：核心线程数 × 3
- [ ] 预计总内存：**~3-5GB**

---

## 🔧 故障排查

### 内存泄漏检测

```bash
# 1. 启用 Heap Dump
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/path/to/dumps

# 2. 使用 MAT (Memory Analyzer Tool) 分析
# 3. 检查 Thread Local 泄漏
# 4. 检查未关闭的资源（连接、流等）
```

### 常见问题

1. **OOM: Java heap space**
   - 增加 `-Xmx` 参数
   - 检查连接池大小
   - 排查内存泄漏

2. **OOM: Metaspace**
   - 增加 `-XX:MaxMetaspaceSize`
   - 检查动态类生成

3. **GC 频繁**
   - 调整新生代/老年代比例
   - 优化对象生命周期
   - 减少临时对象创建

---

## 📝 更新日志

- **2026-03-21**: 初始版本，包含 JVM、连接池、线程池优化配置
