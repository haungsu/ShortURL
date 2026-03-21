# ShortURLPro 宝塔面板部署指南

## 📋 部署前准备

### 1. 环境要求
- Java 21+
- MySQL 8.0+
- Redis 6.0+
- 宝塔面板 7.9+

### 2. 数据库准备

#### 创建MySQL数据库
在宝塔面板中：
1. 进入「数据库」→「MySQL」
2. 点击「添加数据库」
3. 数据库名：`shorturldb`
4. 用户名：建议使用默认的 `root` 或创建新用户
5. 密码：设置并记录好密码

#### Redis配置
在宝塔面板中：
1. 进入「软件商店」
2. 安装 Redis 6.0+
3. 记录 Redis 的端口号（通常是 6379）
4. 如有密码保护，记录 Redis 密码

## 🔧 配置文件修改

### 1. 修改 application-bt.yml
编辑 `application-bt.yml` 文件，填入正确的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturldb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_mysql_username    # 修改为实际用户名
    password: your_mysql_password    # 修改为实际密码
    
  data:
    redis:
      host: localhost                 # Redis主机地址
      port: 6379                      # Redis端口
      password: your_redis_password   # Redis密码（如有）
```

### 2. 在宝塔面板中上传JAR包
1. 打开宝塔面板
2. 进入「网站」→「Java项目」
3. 点击「添加Java项目」
4. 上传编译好的 JAR 包（target/ShortURLPro-0.0.1-SNAPSHOT.jar）

## ⚠️ 宝塔面板常见问题解决

### 问题1：MySQL连接失败
**现象**：提示数据库连接错误
**解决方案**：
1. 确认数据库名正确（应为 `shorturldb`）
2. 检查用户名和密码是否正确
3. 确认MySQL服务正在运行
4. 检查防火墙设置，确保3306端口开放

### 问题2：Redis连接失败
**现象**：提示Redis连接异常
**解决方案**：
1. 确认Redis服务正在运行
2. 检查Redis端口（默认6379）
3. 如设置了密码，确保配置文件中密码正确
4. 检查Redis绑定地址配置

### 问题3：数据库不存在
**现象**：提示找不到数据库
**解决方案**：
1. 在宝塔面板MySQL管理中手动创建数据库
2. 数据库名必须为：`shorturldb`
3. 确保字符集设置为 utf8mb4

## 🚀 启动步骤

### 1. 设置运行参数
在宝塔面板Java项目配置中设置：
```
JVM参数：-Xms512m -Xmx1024m
运行参数：--spring.profiles.active=bt
```

### 2. 启动应用
1. 点击「启动」按钮
2. 观察启动日志
3. 等待应用完全启动（约30-60秒）

### 3. 验证部署
访问：`http://your-domain:8080/actuator/health`
应该返回：`{"status":"UP"}`

## 🔍 故障排查

### 查看启动日志
```bash
# 在宝塔面板中查看应用日志
tail -f /www/wwwlogs/java_project.log
```

### 常见错误及解决方案

#### 错误：Connection refused
```
Caused by: java.net.ConnectException: Connection refused
```
**原因**：数据库或Redis服务未启动
**解决**：在宝塔面板中启动相应的服务

#### 错误：Access denied
```
Access denied for user 'xxx'@'localhost'
```
**原因**：数据库用户名或密码错误
**解决**：检查并修正配置文件中的数据库凭据

#### 错误：Unknown database
```
Unknown database 'shorturldb'
```
**原因**：数据库不存在
**解决**：在MySQL中创建数据库

## 🛠️ 环境变量配置（推荐）

为了更好的安全性，建议使用环境变量：

在宝塔面板Java项目的「环境变量」中添加：
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/shorturldb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
MYSQL_USERNAME=your_username
MYSQL_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

然后在配置文件中使用占位符：
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
```

## 📊 监控配置

### 添加健康检查
在宝塔面板中配置健康检查URL：
```
http://localhost:8080/actuator/health
```

### 性能监控
可以通过以下端点监控应用：
- 健康状态：`/actuator/health`
- 应用信息：`/actuator/info`
- 内存使用：`/actuator/metrics/jvm.memory.used`

## 🔒 安全建议

1. **修改默认管理员密码**
   ```yaml
   spring:
     security:
       user:
         password: your_secure_password  # 修改为强密码
   ```

2. **配置HTTPS**
   在宝塔面板中为域名配置SSL证书

3. **限制访问**
   使用宝塔面板的安全组功能限制端口访问

## 🆘 技术支持

如遇问题，请提供以下信息：
1. 宝塔面板版本
2. 完整的错误日志
3. 当前的配置文件内容
4. 数据库和Redis的运行状态

---
**注意**：首次部署建议先在测试环境中验证配置正确性，再部署到生产环境。