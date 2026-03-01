# GitHub 仓库推送与拉取操作指南

## 📋 目录
1. [准备工作](#准备工作)
2. [推送代码到 GitHub](#推送代码到-github)
3. [从 GitHub 拉取代码](#从-github-拉取代码)
4. [常见问题与解决方案](#常见问题与解决方案)
5. [最佳实践](#最佳实践)

---

## 准备工作

### 1. 检查当前 Git 配置
```bash
# 查看当前用户名和邮箱配置
git config --global user.name
git config --global user.email

# 查看当前远程仓库配置
git remote -v
```

### 2. 当前项目状态确认
您的项目当前状态：
- **远程仓库**: `https://github.com/sabula114514/ShortURLPro.git`
- **本地分支**: `main`
- **仓库状态**: 工作树干净，无待提交更改

### 3. Git 基础配置（如未配置）
```bash
# 配置用户名（替换为您的 GitHub 用户名）
git config --global user.name "your-github-username"

# 配置邮箱（替换为您的 GitHub 注册邮箱）
git config --global user.email "your-email@example.com"

# 验证配置
git config --global user.name
git config --global user.email
```

---

## 推送代码到 GitHub

### 方法一：常规推送（推荐）

#### 1. 添加文件到暂存区
```bash
# 添加所有更改的文件
git add .

# 或者添加特定文件
git add src/main/java/com/example/shorturlpro/YourFile.java
```

#### 2. 提交更改
```bash
# 提交到本地仓库（编写有意义的提交信息）
git commit -m "feat: 添加短链接生成功能"

# 或使用更详细的提交信息
git commit -m "feat: 实现短链接生成API
- 添加ShortUrlGenerateRequest DTO
- 实现Base62编码算法
- 添加冲突检测机制"
```

#### 3. 推送到远程仓库
```bash
# 推送到 main 分支
git push origin main

# 如果是第一次推送，使用 -u 参数建立跟踪关系
git push -u origin main
```

### 方法二：一次性推送所有更改
```bash
# 添加、提交、推送三合一（适用于简单场景）
git add . && git commit -m "update: 同步最新代码" && git push
```

### 提交信息规范
遵循约定式提交格式：
```
<类型>: <简短描述>

[可选的详细描述]

[可选的关联issue]

类型说明：
- feat: 新功能
- fix: 修复bug  
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- perf: 性能优化
- test: 测试相关
- chore: 构建工具或辅助工具变动
```

---

## 从 GitHub 拉取代码

### 1. 拉取最新代码
```bash
# 拉取远程仓库的最新更改
git pull origin main

# 或者分步执行
git fetch origin          # 获取远程更新但不合并
git merge origin/main     # 合并到当前分支
```

### 2. 处理合并冲突
当出现冲突时：
```bash
# 1. 查看冲突文件
git status

# 2. 手动编辑冲突文件，解决冲突标记
# <<<<<<< HEAD
# 本地更改
# =======
# 远程更改  
# >>>>>>> commit-hash

# 3. 标记冲突已解决
git add .
git commit -m "resolve: 解决合并冲突"
```

### 3. 强制拉取（谨慎使用）
```bash
# 丢弃本地更改，强制同步远程代码
git fetch --all
git reset --hard origin/main
```

---

## 常见问题与解决方案

### 🔴 权限问题
**问题**: `remote: Permission to sabula114514/ShortURLPro.git denied`
**解决方案**:
1. 确认您有该仓库的写入权限
2. 检查是否使用正确的凭据
3. 配置 SSH 密钥或使用 Personal Access Token

### 🔴 推送被拒绝
**问题**: `Updates were rejected because the tip of your current branch is behind`
**解决方案**:
```bash
# 先拉取最新代码再推送
git pull origin main
git push origin main
```

### 🔴 文件过大无法推送
**问题**: `remote: error: File xxx is 100.00 MB; this exceeds GitHub's file size limit of 100.00 MB`
**解决方案**:
```bash
# 移除大文件
git rm --cached 大文件名
echo "大文件名" >> .gitignore
git commit -m "chore: 移除大文件并添加到忽略列表"
```

### 🔴 分支不存在
**问题**: `error: src refspec main does not match any`
**解决方案**:
```bash
# 检查本地分支
git branch -a

# 创建并切换到 main 分支
git checkout -b main
```

---

## 最佳实践

### ✅ 日常工作流程
```bash
# 1. 开始工作前先同步最新代码
git pull origin main

# 2. 进行开发工作...

# 3. 提交更改
git add .
git commit -m "描述性的提交信息"

# 4. 推送代码
git push origin main
```

### ✅ 分支管理策略
```bash
# 创建功能分支
git checkout -b feature/new-feature

# 在功能分支上开发...
git add .
git commit -m "feat: 实现新功能"

# 合并到主分支
git checkout main
git pull origin main
git merge feature/new-feature
git push origin main

# 删除已合并的分支
git branch -d feature/new-feature
```

### ✅ 代码审查流程
```bash
# 1. 推送功能分支到远程
git push origin feature/new-feature

# 2. 在 GitHub 上创建 Pull Request

# 3. 根据审查意见修改代码
git add .
git commit -m "address: 根据审查意见修改"
git push origin feature/new-feature

# 4. 审查通过后合并 PR
```

### ⚠️ 注意事项
- **定期推送**: 避免本地积累过多未推送的更改
- **有意义的提交信息**: 便于团队理解和追溯
- **及时拉取**: 避免长时间不同步导致大量冲突
- **备份重要更改**: 重要功能开发前创建分支备份
- **清理无用分支**: 定期清理已合并的功能分支

---

## 项目特定配置

### 当前项目 Git 配置
```bash
# 远程仓库信息
origin  https://github.com/sabula114514/ShortURLPro.git (fetch)
origin  https://github.com/sabula114514/ShortURLPro.git (push)

# 当前分支状态
On branch main
Your branch is up to date with 'origin/main'.
```

### 项目忽略文件
项目已配置 `.gitignore` 文件，自动忽略：
- Maven 构建产物 (`target/`)
- IDE 配置文件 (`.idea/`, `.vscode/`)
- 系统临时文件
- 敏感配置文件

---

## 快速参考命令

| 操作 | 命令 |
|------|------|
| 查看状态 | `git status` |
| 添加文件 | `git add .` |
| 提交更改 | `git commit -m "message"` |
| 推送代码 | `git push origin main` |
| 拉取代码 | `git pull origin main` |
| 查看提交历史 | `git log --oneline` |
| 查看分支 | `git branch -a` |
| 切换分支 | `git checkout branch-name` |

---
📝 **文档更新时间**: 2026年3月1日  
📌 **项目名称**: ShortURL Pro  
🔗 **远程仓库**: https://github.com/sabula114514/ShortURLPro