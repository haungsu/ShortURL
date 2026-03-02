# ShortURL Pro API 接口文档

## 📋 接口概览

本系统提供三类接口：

### 🔓 公开接口（无需认证）
1. `POST /api/short-url/generate` - 生成短链接
2. `GET /{shortCode}` - 短链接跳转

### 🔐 管理员接口（需要JWT认证）
3. `POST /api/auth/login` - 管理员登录
4. `GET /api/short-url/admin/**` - 管理员管理接口

## 🔐 认证机制

### JWT Token认证流程

**1. 登录获取Token**
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**成功响应**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "nickname": "管理员"
}
```

**2. 请求头格式**
所有管理员接口都需要在请求头中携带JWT Token：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**3. Token验证**
```
GET /api/auth/validate
Authorization: Bearer {token}
```

### 安全特性
- ✅ JWT Token有效期2小时
- ✅ Refresh Token有效期7天
- ✅ 基于角色的访问控制(RBAC)
- ✅ CSRF防护已禁用（JWT无状态）
- ✅ CORS跨域支持

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

## 👨‍💼 管理员接口详情

### 管理员登录接口

**接口地址**: `POST /api/auth/login`

**请求头**: 
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**成功响应 (200)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "nickname": "管理员"
}
```

### 管理员短链接管理接口

**基础路径**: `/api/short-url/admin/**`

**通用请求头**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

#### 1. 获取短链接列表
```
GET /api/short-url/admin/list?page=0&size=10&search=关键词&status=ENABLED&sort=createdAt,desc
```

#### 2. 创建短链接
```
POST /api/short-url
{
  "name": "测试链接",
  "originalUrl": "https://example.com",
  "status": "ENABLED"
}
```

#### 3. 更新短链接
```
PUT /api/short-url/admin/{id}
{
  "name": "更新后的名称",
  "originalUrl": "https://updated-example.com",
  "status": "DISABLED"
}
```

#### 4. 删除短链接
```
DELETE /api/short-url/admin/{id}
```

#### 5. 批量删除
```
DELETE /api/short-url/admin/batch
[1, 2, 3, 4, 5]
```

#### 6. 导出数据
```
GET /api/short-url/admin/export?search=关键词&status=ENABLED
```

## 🧪 测试示例

### 使用curl测试

**管理员登录**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**使用Token访问管理员接口**:
```bash
# 先获取Token
token=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq -r '.accessToken')

# 查询短链接列表
curl -X GET "http://localhost:8080/api/short-url/admin/list?page=0&size=5" \
  -H "Authorization: Bearer $token"
```

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

#### 1. 管理员登录
1. **POST请求** `/api/auth/login`
   - Method: POST
   - URL: `http://localhost:8080/api/auth/login`
   - Body: raw JSON
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

2. **保存Token**: 从响应中复制 `accessToken` 值

#### 2. 管理员接口测试
3. **GET请求** `/api/short-url/admin/list`
   - Method: GET
   - URL: `http://localhost:8080/api/short-url/admin/list?page=0&size=10`
   - Headers: 
     - Key: `Authorization`
     - Value: `Bearer [你的accessToken]`

#### 3. 短链接生成测试
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
- ✅ JWT Token认证（RS256算法）
- ✅ 基于角色的访问控制（ROLE_ADMIN）
- ✅ 参数校验和输入过滤
- ✅ URL格式验证
- ✅ 短码唯一性保证
- ✅ 状态检查（启用/禁用）
- ✅ CSRF防护已禁用（JWT无状态）
- ✅ CORS跨域支持

### 性能优化
- ✅ Redis缓存支持（可选）
- ✅ 异步更新点击统计
- ✅ 数据库索引优化

### 可靠性
- ✅ 事务保证数据一致性
- ✅ 异常处理和日志记录
- ✅ 重试机制

## 🛡️ 安全最佳实践

### Token管理
1. **存储安全**
   - 前端使用 `localStorage` 存储Token
   - Token有过期时间，需定期刷新
   - 敏感操作建议使用短期Token

2. **传输安全**
   - 生产环境强制使用HTTPS
   - Token不应出现在URL参数中
   - 敏感接口应有额外的安全措施

3. **权限控制**
   - 管理员接口严格RBAC控制
   - 前端路由级别权限验证
   - 后端接口重复权限校验

### 常见安全风险防范

| 风险类型 | 防范措施 | 当前状态 |
|---------|---------|---------|
| Token泄露 | HTTPS传输、短期有效期 | ✅ 已实现 |
| 权限提升 | RBAC、双重验证 | ✅ 已实现 |
| CSRF攻击 | JWT无状态、SameSite Cookie | ✅ 已实现 |
| 暴力破解 | 登录失败次数限制 | ⚠️ 待实现 |
| 重放攻击 | Token过期机制 | ✅ 已实现 |

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

### 1. 启动应用
```bash
./mvnw spring-boot:run
```

### 2. 管理员登录
```bash
# 获取访问Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 3. 测试管理员接口
```bash
# 使用上一步获得的Token
curl -X GET "http://localhost:8080/api/short-url/admin/list?page=0&size=5" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### 4. 测试公开接口
```bash
# 生成短链接
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com"}'

# 使用返回的短码进行跳转测试
```

### 5. 访问演示页面
浏览器打开: `http://localhost:8080`

## 📞 技术支持

如有问题请联系开发团队或查看系统日志获取更多信息。

### 常见问题排查

**Q: Token过期怎么办？**
A: 使用refreshToken调用`/api/auth/refresh`接口获取新Token

**Q: 管理员接口403错误？**
A: 检查Token是否有效，用户是否具有ROLE_ADMIN角色

**Q: CORS跨域问题？**
A: 确认请求头包含正确的Origin，后端已配置允许跨域

**Q: 如何重置管理员密码？**
A: 修改`application.properties`中的`spring.security.user.password`配置