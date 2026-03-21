# 宝塔面板安装指南

## 系统要求
- 操作系统：CentOS 7.x / Ubuntu 18.04+ / Debian 10+
- 内存：建议 2GB 以上
- 磁盘：建议 50GB 以上可用空间
- 网络：确保服务器可以访问外网

## 安装步骤

### 1. CentOS 系统安装（推荐）

```bash
# 下载并运行官方安装脚本
yum install -y wget && wget -O install.sh http://download.bt.cn/install/install_6.0.sh && sh install.sh ed8484bec
```

### 2. Ubuntu/Debian 系统安装

```bash
# 下载并运行官方安装脚本
wget -O install.sh http://download.bt.cn/install/install-ubuntu_6.0.sh && bash install.sh ed8484bec
```

### 3. 备用安装源

如果官方源无法访问，可以尝试以下备用源：

```bash
# 备用源1
wget -O install.sh http://119.23.220.43:8888/install/install_6.0.sh && bash install.sh ed8484bec

# 备用源2
wget -O install.sh http://123.129.198.192:8888/install/install_6.0.sh && bash install.sh ed8484bec
```

## 安装过程说明

安装过程中会显示：
- 安装进度条
- 需要确认的协议条款（输入 `y` 确认）
- 安装完成后显示登录信息

## 安装完成后的信息

安装成功后，终端会显示类似以下信息：

```
==================================================================
Congratulations! Installed successfully!
==================================================================
外网面板地址: http://your_server_ip:8888/随机字符串
内网面板地址: http://内网IP:8888/随机字符串
username: admin
password: 随机密码
Warning:
If you cannot access the panel,
release the following port (8888|888|80|443|20|21) in the security group
==================================================================
```

## 安全配置

### 1. 修改默认端口
```bash
# 修改面板端口（推荐修改为其他端口）
bt default
# 选择 8 修改端口，然后输入新端口号
```

### 2. 配置防火墙
```bash
# CentOS 7+ (firewalld)
firewall-cmd --permanent --add-port=8888/tcp
firewall-cmd --reload

# Ubuntu/Debian (ufw)
ufw allow 8888/tcp
ufw reload

# 或者直接开放常用端口
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=20/tcp
firewall-cmd --permanent --add-port=21/tcp
firewall-cmd --reload
```

### 3. 云服务商安全组配置
需要在云服务商控制台开放以下端口：
- **8888** - 宝塔面板端口
- **80** - HTTP服务
- **443** - HTTPS服务
- **20/21** - FTP服务
- **3306** - MySQL数据库
- **6379** - Redis服务

## 常用管理命令

```bash
# 查看面板状态
bt status

# 重启面板
bt restart

# 停止面板
bt stop

# 启动面板
bt start

# 修改面板密码
bt 5

# 修改面板用户名
bt 6

# 修改面板端口
bt 8

# 强制修改MySQL密码
bt 10

# 查看面板默认信息
bt default

# 清理系统垃圾
bt 11
```

## 登录面板

1. 打开浏览器访问：`http://你的服务器IP:8888/随机字符串`
2. 使用安装完成后提供的用户名和密码登录
3. 首次登录后建议立即修改密码

## 宝塔面板基础配置

### 1. 安装LNMP环境
登录面板后，在首页选择：
- **LNMP** 一键安装包
- 选择合适的版本组合
- 点击立即安装

### 2. 创建网站
1. 进入「网站」菜单
2. 点击「添加站点」
3. 填写域名、根目录等信息
4. 选择PHP版本

### 3. 数据库管理
1. 进入「数据库」菜单
2. 可以创建MySQL/Redis等数据库
3. 支持在线管理工具

## 部署ShortURLPro项目

### 1. 环境准备
在宝塔面板中安装：
- **Java** (OpenJDK 17+)
- **MySQL** 8.0
- **Redis** 7.x
- **Nginx** 最新稳定版

### 2. 上传项目文件
1. 将ShortURLPro项目打包
2. 通过宝塔文件管理器上传到服务器
3. 解压到指定目录

### 3. 配置数据库
```sql
-- 在宝塔数据库中创建数据库
CREATE DATABASE shorturl_pro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 配置应用
编辑 `application-prod.yml` 文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl_pro?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_db_username
    password: your_db_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 5. Nginx反向代理配置
在宝塔面板中添加站点，然后配置反向代理：
```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 常见问题解决

### 1. 面板无法访问
```bash
# 检查面板是否运行
ps aux | grep bt

# 重启面板服务
bt restart

# 检查端口占用
netstat -tlnp | grep 8888
```

### 2. 忘记面板密码
```bash
# 重置面板密码
bt 5
```

### 3. 面板卡顿或响应慢
```bash
# 清理面板缓存
bt 11

# 重启面板
bt restart
```

### 4. 安全加固
```bash
# 关闭测试账号
bt 34

# 设置面板SSL
bt 13

# 开启BasicAuth认证
bt 33
```

## 卸载宝塔面板

```bash
# CentOS系统卸载
wget http://download.bt.cn/install/bt-uninstall.sh && sh bt-uninstall.sh

# Ubuntu/Debian系统卸载
wget http://download.bt.cn/install/bt-uninstall.sh && sudo bash bt-uninstall.sh
```

## 注意事项

1. **安全性**：安装后务必修改默认端口和密码
2. **备份**：定期备份面板数据和网站数据
3. **监控**：关注服务器资源使用情况
4. **更新**：及时更新面板和插件版本
5. **日志**：保留重要操作的日志记录

## 参考资源

- 宝塔官网：https://www.bt.cn/
- 官方文档：https://www.bt.cn/bbs/forum.php?mod=forumdisplay&fid=36
- 社区支持：https://www.bt.cn/bbs/

---
*本文档基于宝塔面板最新版本编写，具体操作可能因版本更新而有所差异*