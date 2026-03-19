# ShortURL Pro 前端独立启动指南

## 🚀 快速启动

### 方法一：使用启动脚本（推荐）

```bash
# Windows
cd d:\code\sj\ShortURLPro
start-frontend-dev.bat

# 或者进入vue目录直接启动
cd ShortURLPro\vue
start-dev.bat
```

### 方法二：手动启动

```bash
# 1. 进入前端目录
cd ShortURLPro\vue

# 2. 安装依赖（首次运行）
npm install

# 3. 启动开发服务器
npm run dev
```

## 📋 环境要求

- **Node.js**: 20.19+ 或 22.12+
- **npm**: 10.0+ （随Node.js安装）
- **操作系统**: Windows 10+/Linux/macOS

## 🔧 配置说明

### 开发环境配置文件 `.env.development`
```env
# API基础地址
VITE_API_BASE_URL=http://localhost:8080
# 应用名称
VITE_APP_NAME=ShortURL Pro
# 应用版本
VITE_APP_VERSION=1.0.0
```

### Vite配置 `vite.config.ts`
- **端口**: 5173（如被占用会自动切换）
- **代理**: `/api` 请求代理到 `http://localhost:8080`
- **别名**: `@` 指向 `src` 目录

## 🌐 访问地址

启动成功后，前端将在以下地址运行：
- **默认地址**: http://localhost:5173
- **备用地址**: http://localhost:5174（端口被占用时）

## ⚠️ 常见问题

### 1. 端口被占用
```
Port 5173 is in use, trying another one...
```
**解决方案**: Vite会自动切换到下一个可用端口

### 2. 依赖安装失败
```
npm install 失败
```
**解决方案**:
```bash
# 清除缓存
npm cache clean --force
# 删除node_modules重新安装
rm -rf node_modules package-lock.json
npm install
```

### 3. PostCSS配置错误
```
Failed to load PostCSS config: module is not defined
```
**解决方案**: 已修复，确保使用 `export default` 而非 `module.exports`

### 4. Vue文件语法错误
```
Invalid end tag
```
**解决方案**: 已修复所有Vue文件中的无效结束标签

## 🛠️ 开发工具

### 可用的npm脚本
```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行单元测试
npm run test:unit

# 代码检查和修复
npm run lint

# 代码格式化
npm run format
```

## 📁 项目结构

```
ShortURLPro/vue/
├── src/
│   ├── api/          # API请求封装
│   ├── components/   # 公共组件
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理(Pinia)
│   ├── styles/       # 样式文件
│   ├── types/        # TypeScript类型定义
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   ├── App.vue       # 根组件
│   └── main.ts       # 入口文件
├── public/           # 静态资源
├── .env.development  # 开发环境变量
├── .env.production   # 生产环境变量
├── package.json      # 项目依赖
├── vite.config.ts    # Vite配置
└── tsconfig.json     # TypeScript配置
```

## 🔗 后端集成

前端通过以下方式与后端通信：

### API代理配置
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false
    }
  }
}
```

### 环境变量配置
```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
```

## 🎯 功能特性

- ✅ 响应式设计（TailwindCSS）
- ✅ TypeScript类型安全
- ✅ Vue 3 Composition API
- ✅ Pinia状态管理
- ✅ Vue Router路由管理
- ✅ JWT认证集成
- ✅ 开发热重载
- ✅ 生产构建优化

## 📞 技术支持

如遇到启动问题，请检查：
1. Node.js版本是否符合要求
2. 端口是否被其他程序占用
3. 依赖是否完整安装
4. 配置文件是否正确

---
*最后更新: 2026-03-19*