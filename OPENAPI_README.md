# OpenAPI 规范文档

本目录包含了 ShortURL Pro 项目的 OpenAPI 规范文件，用于描述 API 接口的详细信息。

## 文件说明

- `openapi.yaml` - YAML 格式的 OpenAPI 3.0.3 规范文件（推荐用于阅读和编辑）
- `openapi.json` - JSON 格式的 OpenAPI 3.0.3 规范文件（推荐用于程序化处理）

## API 概览

### 核心功能
- 🔗 **短链接生成** - 将长URL转换为短码
- 🔄 **302跳转** - 根据短码重定向到原始链接  
- 📊 **访问统计** - 实时记录点击次数
- 🔐 **用户管理** - 基于Spring Security的权限控制
- 🛠️ **管理功能** - 启用/禁用短链接状态

### 接口分类

#### 1. 短链接服务（公开接口）
- `POST /api/short-url/generate` - 生成短链接
- `GET /{shortCode}` - 短链接跳转

#### 2. 管理接口（需认证）
- `GET /api/short-url/detail/{shortCode}` - 查询短链接详情
- `GET /api/short-url/list` - 查询短链接列表
- `POST /api/short-url` - 创建短链接
- `PUT /api/short-url/{id}` - 更新短链接
- `DELETE /api/short-url/{id}` - 删除短链接
- `PATCH /api/short-url/status/{shortCode}` - 更新短链接状态
- `GET /api/short-url/stats` - 获取系统统计信息
- `GET /admin` - 管理后台首页

#### 3. 认证接口
- `GET /login` - 登录页面
- `POST /login` - 用户登录
- `POST /logout` - 用户登出

## 使用方式

### 1. 在线查看
可以使用以下工具在线查看API文档：

- [Swagger UI](https://editor.swagger.io/) - 粘贴YAML或JSON内容
- [ReDoc](https://redocly.github.io/redoc/) - 更美观的文档展示
- [Postman](https://www.postman.com/) - 导入JSON文件进行API测试

### 2. 本地部署文档
```bash
# 使用 Swagger UI
docker run -d -p 8081:8080 \
  -e SWAGGER_JSON=/openapi.json \
  -v ${PWD}/openapi.json:/openapi.json \
  swaggerapi/swagger-ui

# 使用 ReDoc
npx redoc-cli serve openapi.yaml
```

### 3. 代码生成
使用 OpenAPI Generator 生成客户端代码：

```bash
# 生成 TypeScript 客户端
openapi-generator-cli generate \
  -i openapi.yaml \
  -g typescript-axios \
  -o ./client

# 生成 Java 客户端
openapi-generator-cli generate \
  -i openapi.json \
  -g java \
  -o ./java-client
```

## 技术细节

### 认证方式
- **Basic Auth** - HTTP基本认证
- **Cookie Auth** - 基于Session的认证（JSESSIONID）

### 数据格式
- **请求格式**：application/json, application/x-www-form-urlencoded
- **响应格式**：application/json
- **日期格式**：ISO 8601 (YYYY-MM-DDTHH:mm:ssZ)

### 错误处理
统一错误响应格式：
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "请求参数错误",
  "path": "/api/short-url/generate"
}
```

## 版本信息

- **API版本**：v1.0.0
- **OpenAPI版本**：3.0.3
- **最后更新**：2024年

## 开发规范

### 命名约定
- 路径参数使用驼峰命名法
- 查询参数使用小写下划线命名法
- 枚举值使用大写加下划线

### 状态码规范
- 2xx - 成功响应
- 3xx - 重定向
- 4xx - 客户端错误
- 5xx - 服务器错误

## 维护说明

当API发生变化时，请及时更新对应的OpenAPI规范文件，确保文档与实际接口保持一致。

建议定期使用工具验证OpenAPI文件的有效性：
```bash
# 验证YAML格式
swagger-cli validate openapi.yaml

# 验证JSON格式
swagger-cli validate openapi.json
```