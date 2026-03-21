# 配置文件管理指南

## 配置文件跟踪策略

本项目采用以下Git跟踪策略：

### 跟踪的文件
- `src/main/resources/application-dev-template.yml` - 开发环境配置模板
- `src/main/resources/application-template.yml` - 基础配置模板
- `vue/.env.development` - 前端开发环境配置
- `vue/.env.production` - 前端生产环境配置

### 不跟踪的文件（敏感信息）
- `src/main/resources/application-dev.yml` - 开发环境实际配置
- `src/main/resources/application-prod.yml` - 生产环境实际配置  
- `src/main/resources/application.yml` - 基础实际配置

## 使用方法

### 新开发者设置
```bash
# 克隆项目后，复制模板文件
cp src/main/resources/application-dev-template.yml src/main/resources/application-dev.yml
cp src/main/resources/application-template.yml src/main/resources/application.yml

# 根据实际环境修改配置文件
```

### 前端环境配置
前端环境变量文件已直接跟踪，可根据需要修改：
- `vue/.env.development` - 开发环境
- `vue/.env.production` - 生产环境

## 安全注意事项

1. 实际配置文件包含敏感信息（数据库密码、JWT密钥等），不应提交到版本控制
2. 模板文件只包含示例配置，不包含真实凭据
3. 部署时应使用CI/CD系统或安全的方式分发实际配置文件

## 验证配置
```bash
# 检查Git状态
git status

# 查看被跟踪的配置文件
git ls-files | grep -E "(application|\.env)"
```