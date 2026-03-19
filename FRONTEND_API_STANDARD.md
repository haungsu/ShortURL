# 前端API调用规范

## 1. 统一响应格式

前端已适配后端统一的ApiResponse格式：

```typescript
interface ApiResponse<T> {
  code: number      // 响应码 (200=成功, 400=参数错误, 401=未认证等)
  message: string   // 响应消息
  data: T          // 响应数据
  timestamp: number // 时间戳(毫秒)
}
```

## 2. API调用处理规范

### 2.1 成功响应处理
```typescript
// 标准处理方式
httpClient.post<DataType>('/api/endpoint', data)
  .then(response => {
    // 检查响应码
    if (response.code !== 200) {
      throw new Error(response.message || '操作失败');
    }
    // 返回实际数据
    return response.data;
  })
```

### 2.2 错误处理
```typescript
// 统一错误处理
try {
  const result = await apiCall();
  // 处理成功结果
} catch (error) {
  // 错误信息已在request.ts中统一处理
  console.error('API调用失败:', error.message);
  // 显示用户友好的错误提示
  showMessage(error.message);
}
```

## 3. 各模块适配情况

### 3.1 request.ts (已完成)
- ✅ 添加timestamp字段支持
- ✅ 完善错误响应解析
- ✅ 统一认证失效处理

### 3.2 auth.ts (已完成)
- ✅ login接口添加响应码检查
- ✅ validate接口保持现有逻辑
- ✅ refreshToken接口适配完成

### 3.3 shorturl.ts (已完成)
- ✅ generate接口添加响应验证
- ✅ create接口添加响应码检查
- ✅ getStats接口添加响应码检查
- ✅ update接口添加响应码检查
- ✅ delete接口支持204状态码
- ✅ toggleStatus接口添加响应码检查

### 3.4 AdminView.vue (已完成)
- ✅ loadStats函数适配不同统计字段
- ✅ 统计数据显示兼容性处理
- ✅ 错误边界处理完善

## 4. 响应码对应关系

| 后端响应码 | 前端处理 | HTTP状态码 |
|------------|----------|------------|
| 200 | 成功处理 | 200 OK |
| 204 | 成功处理(无数据) | 204 No Content |
| 400 | 参数错误提示 | 400 Bad Request |
| 401 | 跳转登录页 | 401 Unauthorized |
| 403 | 权限不足提示 | 403 Forbidden |
| 404 | 资源不存在提示 | 404 Not Found |
| 500 | 服务器错误提示 | 500 Internal Server Error |

## 5. 最佳实践

### 5.1 组件中使用API
```vue
<script setup>
import { ref, onMounted } from 'vue'
import { shortUrlApi } from '@/api/shorturl'

const data = ref([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  error.value = ''
  
  try {
    data.value = await shortUrlApi.getList({ page: 0, size: 10 })
  } catch (err) {
    error.value = err.message
    console.error('加载数据失败:', err)
  } finally {
    loading.value = false
  }
}
</script>
```

### 5.2 错误提示处理
```typescript
// 在组件中统一处理错误提示
function handleError(error: Error) {
  // 可以集成通知组件
  if (typeof window !== 'undefined') {
    alert(error.message) // 或使用Toast组件
  }
  console.error('API错误:', error)
}
```

## 6. 测试验证点

### 6.1 功能测试
- [ ] 登录功能正常工作
- [ ] 短链接生成成功
- [ ] 管理员统计数据显示正确
- [ ] 短链接列表分页正常
- [ ] CRUD操作响应及时

### 6.2 错误处理测试
- [ ] 网络错误提示友好
- [ ] 参数错误显示具体信息
- [ ] 认证失效自动跳转
- [ ] 权限不足提示清晰

### 6.3 边界情况测试
- [ ] 空数据列表显示
- [ ] 大量数据分页性能
- [ ] 并发请求处理
- [ ] 断网重连恢复

## 7. 版本变更记录

### v1.0.0 (2024-03-19)
- 🎉 首次适配统一API响应格式
- ✅ 完成request.ts基础框架适配
- ✅ 完成auth.ts认证模块适配
- ✅ 完成shorturl.ts短链接模块适配
- ✅ 完成AdminView.vue管理界面适配
- ✅ 提供完整的前端API调用规范文档