# ShortURL Pro API 接口规范文档

## 📋 文档版本信息
- **版本**: v1.0.0
- **最后更新**: 2024年
- **API规范**: OpenAPI 3.0.3
- **通信协议**: HTTP/HTTPS
- **数据格式**: JSON

## 🎯 统一响应格式

所有接口均遵循统一的响应格式：

### 成功响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1703123456789
}
```

### 错误响应格式
```json
{
  "code": 400,
  "message": "错误描述信息",
  "data": null,
  "timestamp": 1703123456789
}
```

### 响应码说明
| 状态码 | 说明 |
|-------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未认证/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 🔐 认证机制

### JWT Token认证
- **认证方式**: Bearer Token
- **Token类型**: JWT (RS256算法)
- **Access Token有效期**: 2小时
- **Refresh Token有效期**: 7天

### 请求头格式
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

## 📡 接口列表

### 🔓 公开接口（无需认证）

---

## 1. 生成短链接接口

**接口地址**: `POST /api/short-url/generate`

**接口描述**: 将长链接转换为短链接

**权限要求**: 公开接口，无需认证

### 请求参数
```json
{
  "originalUrl": "https://www.example.com/very/long/url/that/needs/to/be/shortened",
  "appId": "myapp"
}
```

### 参数说明
| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| originalUrl | string | 是 | 原始长链接 | 必须以http://或https://开头 |
| appId | string | 否 | 应用标识 | 最大长度50字符 |

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "shortCode": "abc123xyz",
    "shortUrl": "http://localhost:8080/abc123xyz"
  },
  "timestamp": 1703123456789
}
```

### 错误响应
```json
// 400 参数错误
{
  "code": 400,
  "message": "原始链接格式不正确，请以http://或https://开头",
  "data": null,
  "timestamp": 1703123456789
}

// 500 服务器错误
{
  "code": 500,
  "message": "服务器内部错误：生成唯一短码失败，请稍后重试",
  "data": null,
  "timestamp": 1703123456789
}
```

---

## 2. 短链接跳转接口

**接口地址**: `GET /{shortCode}`

**接口描述**: 根据短码进行302重定向到原始链接

**权限要求**: 公开接口，无需认证

### 路径参数
| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| shortCode | string | 是 | 短链接码 | 6位Base62编码字符 |

### 响应说明
- **成功**: 302重定向到原始链接
- **失败**: 404 Not Found

### 示例
```http
GET /abc123xyz
→ 302重定向到 https://www.example.com/original-url
```

---

## 3. 获取系统统计信息

**接口地址**: `GET /api/short-url/stats`

**接口描述**: 获取短链接系统的统计信息

**权限要求**: 需要认证

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 150,
    "totalClicks": 1250,
    "enabledCount": 145,
    "disabledCount": 5
  },
  "timestamp": 1703123456789
}
```

---

## 🔐 认证接口

---

## 4. 用户登录接口

**接口地址**: `POST /api/auth/login`

**接口描述**: 验证用户名密码，生成JWT Token

**权限要求**: 公开接口，无需认证

### 请求参数
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### 参数说明
| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| username | string | 是 | 用户名 | 最小长度3，最大长度20 |
| password | string | 是 | 密码 | 最小长度6，最大长度50 |

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "role": "ROLE_ADMIN",
    "nickname": "管理员"
  },
  "timestamp": 1703123456789
}
```

### 错误响应
```json
// 401 认证失败
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1703123456789
}
```

---

## 5. 刷新Token接口

**接口地址**: `POST /api/auth/refresh`

**接口描述**: 使用刷新Token获取新的访问Token

**权限要求**: 公开接口，无需认证

### 请求参数
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "role": "ROLE_ADMIN",
    "nickname": "管理员"
  },
  "timestamp": 1703123456789
}
```

---

## 6. 验证Token接口

**接口地址**: `GET /api/auth/validate`

**接口描述**: 验证JWT Token的有效性

**权限要求**: 需要认证

### 请求头
```http
Authorization: Bearer {your_token_here}
```

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "username": "admin",
    "role": "ROLE_ADMIN",
    "expiresAt": 1703130656789
  },
  "timestamp": 1703123456789
}
```

---

## 7. 注销接口

**接口地址**: `POST /api/auth/logout`

**接口描述**: 注销当前用户的Token

**权限要求**: 需要认证

### 请求头
```http
Authorization: Bearer {your_token_here}
```

### 请求体（可选）
```json
{
  "tokens": ["token1", "token2"]
}
```

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "注销成功",
  "data": null,
  "timestamp": 1703123456789
}
```

---

## 👨‍💼 管理员接口（需要ROLE_ADMIN权限）

---

## 8. 获取统计信息（管理员权限）

**接口地址**: `GET /api/short-url/admin/stats`

**接口描述**: 管理员获取系统统计信息

**权限要求**: ROLE_ADMIN

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUrls": 150,
    "totalClicks": 1250,
    "todayClicks": 45,
    "activeUrls": 145
  },
  "timestamp": 1703123456789
}
```

---

## 9. 获取短链接列表（管理员权限）

**接口地址**: `GET /api/short-url/admin/list`

**接口描述**: 管理员获取所有短链接列表，支持分页、搜索和筛选

**权限要求**: ROLE_ADMIN

### 查询参数
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| page | integer | 否 | 页码（从0开始） | 0 |
| size | integer | 否 | 每页大小 | 10 |
| search | string | 否 | 搜索关键词 | - |
| status | string | 否 | 状态筛选(ENABLED/DISABLED) | - |
| sort | string | 否 | 排序字段 | createdAt,desc |

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "示例链接",
        "shortCode": "abc123",
        "shortUrl": "http://localhost:8080/abc123",
        "originalUrl": "https://www.example.com",
        "status": "ENABLED",
        "clickCount": 25,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00",
        "userId": 1,
        "appId": "admin",
        "expiresAt": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalElements": 150,
    "totalPages": 15,
    "last": false,
    "first": true,
    "numberOfElements": 10,
    "size": 10,
    "number": 0,
    "empty": false
  },
  "timestamp": 1703123456789
}
```

---

## 10. 创建短链接（管理员权限）

**接口地址**: `POST /api/short-url`

**接口描述**: 管理员创建新的短链接

**权限要求**: ROLE_ADMIN

### 请求参数
```json
{
  "name": "我的短链接",
  "shortCode": "custom123",
  "originalUrl": "https://www.example.com",
  "status": "ENABLED",
  "appId": "admin",
  "expiresAt": "2024-12-31T23:59:59"
}
```

### 参数说明
| 参数名 | 类型 | 必填 | 说明 | 校验规则 |
|--------|------|------|------|----------|
| name | string | 否 | 短链接名称 | 最大长度100字符，默认"未命名" |
| shortCode | string | 否 | 自定义短码 | 6-10位字母/数字 |
| originalUrl | string | 是 | 原始长链接 | 必须以http://或https://开头 |
| status | string | 否 | 状态 | ENABLED/DISABLED，默认ENABLED |
| appId | string | 否 | 应用标识 | 最大长度50字符，默认"admin" |
| expiresAt | string | 否 | 过期时间 | ISO8601格式日期时间 |

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "shortCode": "custom123",
    "shortUrl": "http://localhost:8080/custom123"
  },
  "timestamp": 1703123456789
}
```

---

## 11. 更新短链接（管理员权限）

**接口地址**: `PUT /api/short-url/admin/{id}`

**接口描述**: 管理员更新短链接信息

**权限要求**: ROLE_ADMIN

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 短链接ID |

### 请求参数
```json
{
  "name": "更新后的名称",
  "originalUrl": "https://www.updated-example.com",
  "status": "DISABLED"
}
```

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "更新后的名称",
    "shortCode": "abc123",
    "shortUrl": "http://localhost:8080/abc123",
    "originalUrl": "https://www.updated-example.com",
    "status": "DISABLED",
    "clickCount": 25,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-02T10:00:00",
    "userId": 1,
    "appId": "admin",
    "expiresAt": null
  },
  "timestamp": 1703123456789
}
```

---

## 12. 删除短链接（管理员权限）

**接口地址**: `DELETE /api/short-url/admin/{id}`

**接口描述**: 管理员删除短链接

**权限要求**: ROLE_ADMIN

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 短链接ID |

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1703123456789
}
```

### 错误响应
```json
// 404 短链接不存在
{
  "code": 404,
  "message": "短链接不存在，ID: 999",
  "data": null,
  "timestamp": 1703123456789
}
```

---

## 13. 批量删除短链接（管理员权限）

**接口地址**: `DELETE /api/short-url/admin/batch`

**接口描述**: 管理员批量删除短链接

**权限要求**: ROLE_ADMIN

### 请求参数
```json
[1, 2, 3, 4, 5]
```

### 成功响应 (200)
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": {
    "deletedCount": 5,
    "failedIds": []
  },
  "timestamp": 1703123456789
}
```

---

## 14. 导出短链接数据（管理员权限）

**接口地址**: `GET /api/short-url/admin/export`

**接口描述**: 导出短链接数据为Excel文件

**权限要求**: ROLE_ADMIN

### 查询参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| search | string | 否 | 搜索关键词 |
| status | string | 否 | 状态筛选 |

### 响应说明
- **成功**: 返回Excel文件下载
- **Content-Type**: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

---

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
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq -r '.data.accessToken')

# 查询短链接列表
curl -X GET "http://localhost:8080/api/short-url/admin/list?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

**生成短链接**:
```bash
curl -X POST http://localhost:8080/api/short-url/generate \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.github.com"}'
```

### 使用Postman测试

#### 1. 管理员登录
- **Method**: POST
- **URL**: `http://localhost:8080/api/auth/login`
- **Body**: 
```json
{
  "username": "admin",
  "password": "admin123"
}
```

#### 2. 短链接生成
- **Method**: POST
- **URL**: `http://localhost:8080/api/short-url/generate`
- **Body**:
```json
{
  "originalUrl": "https://www.baidu.com",
  "appId": "test"
}
```

## 📊 错误码对照表

| 错误码 | HTTP状态码 | 说明 | 解决方案 |
|--------|------------|------|----------|
| 1001 | 400 | 参数校验失败 | 检查请求参数格式和必填项 |
| 1002 | 400 | URL格式不正确 | 确保URL以http://或https://开头 |
| 1003 | 400 | 短码格式不正确 | 短码必须是6-10位字母/数字 |
| 2001 | 401 | 未提供认证信息 | 添加Authorization请求头 |
| 2002 | 401 | Token无效 | 重新登录获取新Token |
| 2003 | 401 | Token已过期 | 使用refreshToken刷新或重新登录 |
| 3001 | 403 | 权限不足 | 确认用户具有相应角色权限 |
| 4001 | 404 | 资源不存在 | 检查资源ID是否正确 |
| 5001 | 500 | 服务器内部错误 | 查看服务器日志，联系技术支持 |

## 🔧 技术规格

### 系统要求
- **Java版本**: 17+
- **Spring Boot版本**: 3.x
- **数据库**: MySQL 8.0+/H2
- **缓存**: Redis (可选)
- **构建工具**: Maven 3.8+

### 部署环境
- **开发环境**: http://localhost:8080
- **生产环境**: https://api.shorturlpro.com

### 性能指标
- **平均响应时间**: < 200ms
- **并发处理能力**: 1000+ QPS
- **缓存命中率**: > 90%

## 📞 技术支持

如有问题请：
1. 查看系统日志获取详细错误信息
2. 确认请求参数符合规范要求
3. 验证Token是否有效且未过期
4. 联系开发团队获取进一步支持