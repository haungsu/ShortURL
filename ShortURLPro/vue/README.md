# ShortURL Pro 前端项目

基于 Vue 3 + TypeScript + TailwindCSS 的现代化短链接管理系统前端。

## 技术栈

- **框架**: Vue 3.5 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite 5
- **路由**: Vue Router 5
- **状态管理**: Pinia
- **样式**: TailwindCSS 3
- **HTTP客户端**: Axios + 自定义封装
- **开发工具**: ESLint, Prettier, Vitest

## 项目结构

```
src/
├── api/           # API接口封装
│   ├── auth.ts    # 认证相关API
│   ├── request.ts # HTTP请求封装
│   └── shorturl.ts # 短链接相关API
├── components/    # 公共组件
├── router/        # 路由配置
│   └── index.ts
├── stores/        # 状态管理
│   └── auth.ts    # 认证状态
├── styles/        # 样式文件
│   └── tailwind.css
├── utils/         # 工具函数
│   └── helpers.ts
├── views/         # 页面组件
│   ├── AdminView.vue  # 管理后台
│   ├── HomeView.vue   # 首页
│   └── LoginView.vue  # 登录页
├── App.vue        # 根组件
└── main.ts        # 入口文件
```

## 功能特性

### 🏠 首页功能
- 短链接生成器
- 用户认证状态显示
- 管理员登录入口
- 响应式设计

### 🔐 认证系统
- JWT Token认证
- 管理员角色控制
- 登录状态持久化
- 自动Token刷新

### 📊 管理后台
- 短链接列表管理
- 实时统计数据展示
- 搜索和筛选功能
- 数据导出功能
- 状态切换和删除操作

## 开发环境搭建

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

默认访问地址: http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 环境配置

项目支持多环境配置：

- `.env.development` - 开发环境
- `.env.production` - 生产环境

关键环境变量：
```bash
VITE_API_BASE_URL=http://localhost:8080  # 后端API地址
VITE_APP_NAME=ShortURL Pro               # 应用名称
VITE_APP_VERSION=1.0.0                   # 应用版本
```

## 代码规范

### ESLint 配置
```bash
npm run lint
```

### Prettier 格式化
```bash
npm run format
```

## 测试

### 单元测试
```bash
npm run test:unit
```

### 测试覆盖率
```bash
npm run test:unit -- --coverage
```

## 部署

### Docker部署
```bash
# 构建镜像
docker build -t shorturl-frontend .

# 运行容器
docker run -p 80:80 shorturl-frontend
```

### 静态文件部署
构建完成后，将 `dist` 目录下的文件部署到任意静态文件服务器即可。

## API接口

前端通过以下API与后端交互：

### 认证接口
- `POST /api/auth/login` - 管理员登录
- `GET /api/auth/validate` - 验证Token
- `POST /api/auth/refresh` - 刷新Token

### 短链接接口
- `POST /api/short-url/generate` - 生成短链接（公开）
- `POST /api/short-url` - 创建短链接（管理员）
- `GET /api/short-url/admin/list` - 获取短链接列表
- `GET /api/short-url/admin/stats` - 获取统计数据
- `PUT /api/short-url/{id}` - 更新短链接
- `DELETE /api/short-url/{id}` - 删除短链接
- `PATCH /api/short-url/{id}/status` - 切换状态

## 浏览器支持

- Chrome 80+
- Firefox 74+
- Safari 13+
- Edge 80+

## 注意事项

1. 确保后端服务正常运行
2. 开发环境下API请求会代理到 `http://localhost:8080`
3. 生产环境需要正确配置 `VITE_API_BASE_URL`
4. 默认管理员账号: `admin` / `admin123`

## 许可证

MIT License