# ShortURL Pro

## 项目简介

ShortURL Pro 是一个基于 Spring Boot 的短链接服务系统，支持链接缩短、访问统计、用户管理等功能。

## 本地部署

### MySQL配置

1.进sql

先确保你已经安装了MySQL

运行以下命令
```bash
mysql -u root -p
```
输入你的SQL密码

2. 运行以下代码
```sql
CREATE DATABASE IF NOT EXISTS short_url_pro
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

## GitHub推送

本项目已配置自动推送脚本，支持多种平台：

### Windows系统
```powershell
# PowerShell脚（推荐（推荐）
.\push_to_github.ps1

# 批处理脚本（兼容性更好）
.\push_to_github.bat
```

### Linux/Mac系统
```bash
#给本脚本添加执行权限
chmod +x push_to_github.sh

#执行推送
./push_to_github.sh
```

###手动推送
```bash
#确保在 hs 分支
git checkout hs

# 添加所有更改
git add -A

# 提交更改
git commit -m "更新 ShortURLPro 项目文件"

#推送到 GitHub
git push origin hs
```

###推送目标
- **仓库地址**: https://github.com/sabula114514/ShortURLPro
- **目标分支**: hs
- **完整URL**: https://github.com/sabula114514/ShortURLPro/tree/hs

详细使用说明请查看 [GITHUB_PUSH_GUIDE.md](GITHUB_PUSH_GUIDE.md)