# ShortURL Pro API 接口文档

## 📋 接口概览

本系统提供两个核心公开接口：
1. `POST /api/short-url/generate` - 生成短链接
2. `GET /{shortCode}` - 短链接跳转

## 🔗 接口详情

### 1. 生成短链接接口

**接口地址**: `POST /api/short-url/generate`

**请求头**: 
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "originalUrl": "https://www.example.com/very/long/url/that/needs/to/be/shortened",
  "appId": "myapp"  // 可选参数
}
```

**参数说明**:
- `originalUrl` (必填): 原始长链接，必须以 http:// 或 https:// 开头
- `appId` (可选): 应用标识，用于区分不同应用的短链接

**成功响应 (200)**:
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123"
}
```

**错误响应**:
```json
// 400 参数错误
{
  "code": 400,
  "message": "原始链接格式不正确，请以http://或https://开头"
}

// 500 服务器错误
{
  "code": 500,
  "message": "服务器内部错误：生成唯一短码失败，请稍后重试"
}
```

### 2. 短链接跳转接口

**接口地址**: `GET /{shortCode}`

**路径参数**:
- `shortCode`: 短链接码（6位Base62编码）

**响应**:
- 成功: 302重定向到原始链接
- 失败: 404 Not Found

**示例**:
```
GET /abc123
→ 302重定向到 https://www.example.com/original-url
```

## 🧪 测试示例

### 使用curl测试

**生成短链接**:
```bash
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.github.com"}'
```

**跳转短链接**:
```bash
curl -v http://localhost:8080/abc123
```

### 使用Postman测试

1. **POST请求** `/api/short-url/generate`
   - Method: POST
   - URL: `http://localhost:8080/api/short-url/generate`
   - Body: raw JSON
   ```json
   {
     "originalUrl": "https://www.baidu.com",
     "appId": "test"
   }
   ```

2. **GET请求** `/{shortCode}`
   - Method: GET
   - URL: `http://localhost:8080/[返回的shortCode]`

## 🔧 技术特性

### 安全性
- ✅ 参数校验和输入过滤
- ✅ URL格式验证
- ✅ 短码唯一性保证
- ✅ 状态检查（启用/禁用）

### 性能优化
- ✅ Redis缓存支持（可选）
- ✅ 异步更新点击统计
- ✅ 数据库索引优化

### 可靠性
- ✅ 事务保证数据一致性
- ✅ 异常处理和日志记录
- ✅ 重试机制

## 📊 系统监控

### 访问统计
每次短链接跳转都会自动增加点击次数统计。

### 日志级别
- DEBUG: 详细调试信息
- INFO: 一般业务流程
- WARN: 警告信息
- ERROR: 错误信息

## ⚙️ 配置说明

### 应用配置
```properties
# 服务器端口
server.port=8080

# 应用域名（生成完整短链接时使用）
app.domain=http://localhost:8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/short_url_pro
spring.datasource.username=root
spring.datasource.password=123456
```

### 开发环境快速启动
使用H2内存数据库：
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

## 🚀 快速开始

1. **启动应用**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **测试接口**:
   ```bash
   # 生成短链接
   curl -X POST http://localhost:8080/api/short-url/generate \
     -H "Content-Type: application/json" \
     -d '{"originalUrl": "https://www.google.com"}'
   
   # 使用返回的短码进行跳转测试
   ```

3. **访问演示页面**:
   浏览器打开: `http://localhost:8080`

## 📞 技术支持

如有问题请联系开发团队或查看系统日志获取更多信息。