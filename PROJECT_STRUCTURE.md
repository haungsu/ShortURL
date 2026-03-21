# ShortURL Pro 项目结构说明

## 📁 目录结构概览

本项目按照功能域进行分类组织，使文件管理更加清晰和便于维护。

## 🗂️ 各目录用途说明

### 📁 `src/` - 核心源代码
存放项目的主要源代码文件
- `main/java/` - Java后端源码
- `main/resources/` - 配置文件和静态资源

### 📁 `vue/` - 前端项目
完整的Vue.js前端应用程序
- `src/` - 前端源代码
- `public/` - 静态资源文件
- `package.json` - 前端依赖管理

### 📁 `deployment/` - 部署相关文件
所有与部署相关的配置和脚本

#### `deployment/docker/`
- `docker-compose.dev.yml` - 开发环境Docker编排
- `docker-compose.nginx.yml` - 生产环境Docker编排

#### `deployment/nginx/`
- `conf.d/` - Nginx站点配置文件
- `ssl/` - SSL证书文件
- `nginx.conf` - Nginx主配置文件
- `generate-ssl.*` - SSL证书生成脚本

#### `deployment/scripts/`
- `deploy-bt.bat` - 宝塔面板部署脚本
- `deploy-nginx.bat` - Nginx部署脚本

### 📁 `docs/` - 文档资料
项目相关的所有文档文件

#### `docs/guides/` - 使用指南
- `CONFIGURATION_GUIDE.md` - 配置说明指南
- `STARTUP_INSTRUCTIONS.md` - 启动操作说明

#### `docs/references/` - 技术参考
- `TECH_STACK_DOCUMENTATION.md` - 技术栈详细文档

#### `docs/deployment/` - 部署文档
- `BT_PANEL_DEPLOYMENT.md` - 宝塔面板部署指南
- `DOCKER_DEPLOYMENT_GUIDE.md` - Docker部署详细说明
- `NGINX_DEPLOYMENT_GUIDE.md` - Nginx部署指南
- `DEPLOYMENT_GUIDE.md` - 通用部署指南

### 📁 `infrastructure/` - 基础设施
项目运行所需的基础组件和工具

#### `infrastructure/database/`
- `init-bt.sql` - 数据库初始化脚本

#### `infrastructure/cli/`
- 命令行工具和自动化脚本
- `skills_store_cli.py` - 技能管理工具
- `install.sh` - 安装脚本

#### `infrastructure/monitoring/`
- 监控系统配置文件（Prometheus、Grafana等）

### 📁 `tools/` - 开发工具
开发过程中使用的辅助工具和脚本

#### `tools/scripts/`
- 各种开发辅助脚本

### 📁 `config/` - 配置模板
环境配置文件模板
- `application-bt.yml` - 宝塔面板配置模板

## 📋 文件分类原则

### 1. 功能相关性
相同功能的文件归类到同一目录下，便于查找和维护

### 2. 环境隔离
不同环境（开发、测试、生产）的配置文件分开存放

### 3. 版本控制友好
区分需要和不需要Git跟踪的文件，避免敏感信息泄露

### 4. 团队协作便利
清晰的目录结构便于团队成员理解和协作开发

## 🔄 迁移历史

本项目于2026年3月20日完成了从扁平化结构到功能域分类结构的重构：

**迁移前结构：**
```
ShortURLPro/
├── docker-compose.*.yml
├── nginx/
├── deploy-*.bat
├── *.md 文档文件
├── sql/
├── cli/
├── monitoring/
└── application-*.yml
```

**迁移后结构：**
```
ShortURLPro/
├── deployment/
├── docs/
├── infrastructure/
├── tools/
└── config/
```

## 📝 维护建议

1. **新增文件时**：根据文件功能选择合适的目录存放
2. **文档更新时**：及时更新对应的文档文件
3. **配置变更时**：优先修改模板文件，再应用到具体环境
4. **定期清理**：及时清理过期的临时文件和备份文件

## 🔍 快速导航

- 🚀 **快速开始**：查看 `docs/guides/STARTUP_INSTRUCTIONS.md`
- ⚙️ **配置说明**：查看 `docs/guides/CONFIGURATION_GUIDE.md`  
- 🐳 **Docker部署**：查看 `docs/deployment/DOCKER_DEPLOYMENT_GUIDE.md`
- 📊 **技术栈**：查看 `docs/references/TECH_STACK_DOCUMENTATION.md`
- 🔧 **宝塔部署**：查看 `docs/deployment/BT_PANEL_DEPLOYMENT.md`

---
*本文档最后更新：2026年3月20日*