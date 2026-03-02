# ShortURL Pro 所有网址清单

## 🌐 基础信息

**项目名称**: ShortURL Pro 短链接服务平台  
**版本**: 1.0.0  
**基础URL**: http://localhost:8080  

---

## 📋 URL分类总览

| 分类 | 数量 | 描述 |
|------|------|------|
| 🌍 公开接口 | 3个 | 无需认证即可访问 |
| 🔐 认证接口 | 5个 | 需要JWT Token认证 |
| 👨‍💼 管理员接口 | 10个 | 需要ROLE_ADMIN权限 |
| 🖥️ 页面路由 | 4个 | 前端页面访问路径 |

---

## 🌍 公开接口 (Public APIs)

### 1. 生成短链接
```
POST /api/short-url/generate
```
**描述**: 将长链接转换为短链接  
**请求体**: 
```json
{
  "originalUrl": "https://www.example.com/very/long/url",
  "appId": "myapp"  // 可选
}
```
**响应示例**:
```json
{
  "shortCode": "abc123xyz",
  "shortUrl": "http://localhost:8080/abc123xyz"
}
```

### 2. 短链接跳转
```
GET /{shortCode}
```
**描述**: 根据短码302重定向到原始链接  
**路径参数**: `shortCode` (6位Base62编码)  
**示例**: `GET /abc123` → 302重定向到原始链接

### 3. 登录页面
```
GET /login
```
**描述**: 显示用户登录界面  
**响应类型**: HTML页面

---

## 🔐 认证接口 (Authentication APIs)

### 1. 用户登录
```
POST /api/auth/login
```
**描述**: 用户登录认证，获取JWT Token  
**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
**响应示例**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "nickname": "管理员"
}
```

### 2. 刷新Token
```
POST /api/auth/refresh
```
**描述**: 使用刷新Token获取新的访问Token  
**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. 验证Token
```
GET /api/auth/validate
```
**描述**: 验证JWT Token的有效性  
**请求头**: `Authorization: Bearer {token}`

### 4. 用户注销
```
POST /api/auth/logout
```
**描述**: 注销当前用户的Token  
**请求头**: `Authorization: Bearer {token}`

### 5. 表单登录
```
POST /login
```
**描述**: 传统的表单登录方式  
**请求体**: `application/x-www-form-urlencoded`
```
username=admin&password=admin123
```

---

## 👨‍💼 管理员接口 (Admin APIs)

### 1. 获取短链接详情
```
GET /api/short-url/detail/{shortCode}
```
**描述**: 根据短码查询短链接详细信息  
**权限**: 需要认证  
**路径参数**: `shortCode`

### 2. 查询短链接列表
```
GET /api/short-url/list
```
**描述**: 分页查询短链接列表  
**权限**: 需要认证  
**查询参数**:
- `name`: 名称搜索关键词
- `page`: 页码(默认0)
- `size`: 每页大小(默认10)

### 3. 创建短链接
```
POST /api/short-url
```
**描述**: 创建新的短链接记录  
**权限**: 需要认证  
**请求体**:
```json
{
  "name": "我的网站",
  "originalUrl": "https://www.example.com",
  "appId": "myapp",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

### 4. 更新短链接
```
PUT /api/short-url/{id}
```
**描述**: 根据ID更新短链接信息  
**权限**: 需要认证  
**路径参数**: `id`

### 5. 删除短链接
```
DELETE /api/short-url/{id}
```
**描述**: 根据ID删除短链接记录  
**权限**: 需要认证  
**路径参数**: `id`

### 6. 更新短链接状态
```
PATCH /api/short-url/status/{shortCode}
```
**描述**: 启用或禁用指定的短链接  
**权限**: 需要认证  
**路径参数**: `shortCode`  
**查询参数**: `status=ENABLED|DISABLED`

### 7. 获取系统统计
```
GET /api/short-url/stats
```
**描述**: 获取系统的总体统计信息  
**权限**: 需要认证  
**响应示例**:
```json
{
  "totalLinks": 100,
  "activeLinks": 85,
  "totalClicks": 1250,
  "todayClicks": 45
}
```

### 8. 管理员获取列表
```
GET /api/short-url/admin/list
```
**描述**: 管理员专用的短链接列表查询  
**权限**: ROLE_ADMIN  
**查询参数**:
- `page`: 页码
- `size`: 每页大小
- `search`: 搜索关键词
- `status`: 状态筛选
- `sort`: 排序字段

### 9. 管理员创建短链接
```
POST /api/short-url/admin
```
**描述**: 管理员创建短链接  
**权限**: ROLE_ADMIN

### 10. 管理员更新短链接
```
PUT /api/short-url/admin/{id}
```
**描述**: 管理员更新短链接  
**权限**: ROLE_ADMIN  
**路径参数**: `id`

### 11. 管理员删除短链接
```
DELETE /api/short-url/admin/{id}
```
**描述**: 管理员删除短链接  
**权限**: ROLE_ADMIN  
**路径参数**: `id`

### 12. 批量删除短链接
```
DELETE /api/short-url/admin/batch
```
**描述**: 管理员批量删除短链接  
**权限**: ROLE_ADMIN  
**请求体**: `[1, 2, 3]` (ID数组)

### 13. 导出短链接数据
```
GET /api/short-url/admin/export
```
**描述**: 导出短链接数据为Excel文件  
**权限**: ROLE_ADMIN  
**查询参数**:
- `search`: 搜索关键词
- `status`: 状态筛选

---

## 🖥️ 页面路由 (Web Pages)

### 1. 首页/演示页
```
GET /
```
**描述**: 短链接生成演示页面  
**文件位置**: `src/main/resources/static/index.html`

### 2. 登录页面
```
GET /login
```
**描述**: 用户登录页面  
**文件位置**: `src/main/resources/static/login.html`

### 3. 管理后台
```
GET /admin
```
**描述**: 管理员后台管理页面  
**权限**: ROLE_ADMIN  
**文件位置**: `src/main/resources/static/admin-manage.html`

### 4. 用户登出
```
POST /logout
```
**描述**: 用户登出，清除会话并重定向到首页

---

## 🛠️ 开发工具接口

### Swagger UI 文档
```
GET /swagger-ui.html
```
**描述**: API接口文档页面

### OpenAPI 规范
```
GET /v3/api-docs
```
**描述**: OpenAPI 3.0 规范JSON

---

## 🔧 WebSocket接口

### 实时通知
```
WebSocket /ws/notifications
```
**描述**: 实时推送系统通知和统计数据更新  
**权限**: 需要认证

---

## 📱 移动端接口

### 移动端登录
```
POST /api/mobile/login
```
**描述**: 移动端专用登录接口  
**权限**: 公开接口

### 移动端短链接列表
```
GET /api/mobile/short-urls
```
**描述**: 移动端获取用户短链接列表  
**权限**: 需要认证

---

## 🎯 使用示例

### 1. 生成短链接 (curl)
```bash
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com"}'
```

### 2. 跳转短链接 (浏览器)
```
http://localhost:8080/abc123xyz
```

### 3. 管理员登录 (Postman)
```
POST http://localhost:8080/api/auth/login
Body: {
  "username": "admin",
  "password": "admin123"
}
```

### 4. 查询短链接列表 (带认证)
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/short-url/list?page=0&size=10
```

---

## 🔐 安全说明

1. **公开接口**: 无需认证，直接访问
2. **认证接口**: 需要有效的JWT Token
3. **管理员接口**: 需要ROLE_ADMIN角色权限
4. **Token有效期**: 
   - Access Token: 2小时
   - Refresh Token: 7天

---

## 📊 状态码说明

| 状态码 | 描述 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 204 | 删除成功 |
| 302 | 重定向 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 📞 技术支持

**项目仓库**: https://github.com/your-org/ShortURLPro  
**Wiki文档**: https://github.com/your-org/ShortURLPro/wiki  
**联系邮箱**: support@shorturlpro.com  

---
*文档版本: 1.0.0*  
*最后更新: 2026-03-02*