# API返回数据规范

## 1. 统一响应格式

所有API接口均采用统一的响应格式，使用`ApiResponse<T>`泛型类进行封装。

### 1.1 响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710835200000
}
```

### 1.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | Integer | 是 | 响应码，与HTTP状态码一致 |
| message | String | 是 | 响应消息描述 |
| data | Object | 否 | 响应数据，成功时返回具体数据，失败时可能为空 |
| timestamp | Long | 是 | 时间戳（毫秒） |

## 2. 响应码规范

### 2.1 成功响应 (2xx)

- `200`: 操作成功
- `201`: 创建成功
- `204`: 删除成功（无返回数据）

### 2.2 客户端错误 (4xx)

- `400`: 请求参数错误
- `401`: 未认证/Token无效
- `403`: 权限不足
- `404`: 资源不存在
- `422`: 参数校验失败

### 2.3 服务器错误 (5xx)

- `500`: 服务器内部错误
- `503`: 服务不可用

## 3. 使用示例

### 3.1 成功响应

```java
// 成功响应（无数据）
return ResponseEntity.ok(ApiResponse.success());

// 成功响应（带数据）
return ResponseEntity.ok(ApiResponse.success(userData));

// 成功响应（自定义消息）
return ResponseEntity.ok(ApiResponse.success("创建成功", createdData));
```

### 3.2 错误响应

```java
// 参数错误
return ResponseEntity.badRequest()
    .body(ApiResponse.badRequest("参数不能为空"));

// 未认证
return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    .body(ApiResponse.unauthorized("Token已过期"));

// 权限不足
return ResponseEntity.status(HttpStatus.FORBIDDEN)
    .body(ApiResponse.forbidden("权限不足"));

// 资源不存在
return ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ApiResponse.notFound("资源不存在"));

// 服务器错误
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ApiResponse.error("数据库连接失败"));
```

## 4. 各控制器改造情况

### 4.1 AdminController
- ✅ `getStats()`: 统计信息接口
- ✅ `getAllShortUrls()`: 分页查询接口  
- ✅ `createShortUrl()`: 创建短链接接口
- ✅ `updateShortUrl()`: 更新短链接接口
- ✅ `deleteShortUrl()`: 删除短链接接口
- ✅ `batchDeleteShortUrls()`: 批量删除接口

### 4.2 AuthController
- ✅ `logout()`: 注销接口

### 4.3 ShortUrlController
- ✅ `generateShortUrl()`: 生成短链接接口
- ✅ `getStats()`: 统计信息接口

### 4.4 GlobalExceptionHandler
- ✅ 参数校验异常处理
- ✅ 认证异常处理
- ✅ 权限异常处理
- ✅ Token异常处理
- ✅ 运行时异常处理
- ✅ 通用异常处理

## 5. 前后端对接说明

### 5.1 前端判断逻辑

```javascript
// 判断请求是否成功
if (response.code === 200) {
  // 处理成功数据
  console.log('操作成功:', response.data);
} else {
  // 处理错误情况
  console.error('操作失败:', response.message);
}

// 或者通过HTTP状态码判断
if (response.status === 200) {
  const result = response.data;
  if (result.code === 200) {
    // 成功处理
  } else {
    // 错误处理
  }
}
```

### 5.2 注意事项

1. **时间戳字段**: `timestamp`为毫秒级时间戳，可用于前端显示相对时间
2. **空数据处理**: 当`data`为null时，表示该接口不需要返回具体数据
3. **错误详情**: 对于参数校验错误，详细信息包含在`message`字段中
4. **兼容性**: 新的响应格式向后兼容，原有的字段含义保持不变

## 6. 测试验证

建议对以下场景进行测试：

1. 正常业务流程（200响应）
2. 参数校验失败（400响应）
3. 认证失败（401响应）
4. 权限不足（403响应）
5. 资源不存在（404响应）
6. 服务器内部错误（500响应）

## 7. 版本变更记录

### v1.0.0 (2024-03-19)
- 🎉 首次统一API响应格式
- ✅ 添加ApiResponse统一响应类
- ✅ 改造所有控制器接口
- ✅ 统一全局异常处理
- ✅ 提供完整的使用文档