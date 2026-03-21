@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: ============================================
:: ShortURLPro 宝塔面板部署脚本
:: ============================================

title ShortURLPro 宝塔面板部署助手

echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║           ShortURLPro 宝塔面板部署助手               ║
echo ╚══════════════════════════════════════════════════════╝
echo.

:: 检查Java环境
echo [检查] 正在检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Java环境，请先安装Java 21+
    echo 💡 建议：在宝塔面板软件商店中安装OpenJDK 21
    pause
    exit /b 1
) else (
    echo ✅ Java环境正常
    java -version
)

:: 检查Maven环境
echo.
echo [检查] 正在检查Maven环境...
mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  未检测到Maven环境，将跳过编译步骤
    echo 💡 建议：在宝塔面板软件商店中安装Maven
) else (
    echo ✅ Maven环境正常
)

:: 检查配置文件
echo.
echo [检查] 正在检查配置文件...
if exist "application-bt.yml" (
    echo ✅ 发现宝塔面板专用配置文件
) else (
    echo ❌ 未找到 application-bt.yml 配置文件
    echo 💡 请确保该文件存在于项目根目录
    pause
    exit /b 1
)

:: 编译项目（如果Maven可用）
echo.
echo [编译] 是否需要重新编译项目？(y/n)
set /p compile_choice=
if /i "%compile_choice%"=="y" (
    echo 正在编译项目...
    if exist "mvnw.cmd" (
        call mvnw.cmd clean package -DskipTests
    ) else (
        mvn clean package -DskipTests
    )
    
    if %errorlevel% neq 0 (
        echo ❌ 项目编译失败
        pause
        exit /b 1
    ) else (
        echo ✅ 项目编译成功
    )
)

:: 检查JAR包
echo.
echo [检查] 正在检查可执行JAR包...
if exist "target\ShortURLPro-0.0.1-SNAPSHOT.jar" (
    echo ✅ 找到可执行JAR包
    set JAR_FILE=target\ShortURLPro-0.0.1-SNAPSHOT.jar
) else (
    echo ❌ 未找到可执行JAR包
    echo 💡 请先编译项目或确认JAR包位置
    pause
    exit /b 1
)

:: 部署配置指导
echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║              宝塔面板部署配置指导                     ║
echo ╚══════════════════════════════════════════════════════╝
echo.

echo 请按以下步骤在宝塔面板中配置：

echo.
echo 1. 【数据库配置】
echo    - 登录宝塔面板
echo    - 进入「数据库」→「MySQL」
echo    - 创建数据库：shorturldb
echo    - 记录用户名和密码

echo.
echo 2. 【Redis配置】
echo    - 在宝塔面板「软件商店」中安装Redis
echo    - 记录Redis端口（通常为6379）
echo    - 如有密码保护，记录Redis密码

echo.
echo 3. 【修改配置文件】
echo    请编辑 application-bt.yml 文件，填入正确的配置：
echo    - MySQL用户名和密码
echo    - Redis主机和端口
echo    - Redis密码（如有）

echo.
echo 4. 【宝塔面板部署】
echo    - 进入「网站」→「Java项目」
echo    - 点击「添加Java项目」
echo    - 上传JAR包：%JAR_FILE%
echo    - 设置运行参数：--spring.profiles.active=bt
echo    - 配置端口：8080

pause

:: 显示配置示例
echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║              application-bt.yml 配置示例             ║
echo ╚══════════════════════════════════════════════════════╝
echo.

echo spring:
echo   datasource:
echo     url: jdbc:mysql://localhost:3306/shorturldb?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai^&allowPublicKeyRetrieval=true
echo     username: your_mysql_username    # ← 修改为实际用户名
echo     password: your_mysql_password    # ← 修改为实际密码
echo     
echo   data:
echo     redis:
echo       host: localhost                 # ← Redis主机地址
echo       port: 6379                      # ← Redis端口
echo       password: your_redis_password   # ← Redis密码（如有）

echo.
echo 💡 配置完成后，在宝塔面板中重启Java项目即可

echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║                    部署完成                          ║
echo ╚══════════════════════════════════════════════════════╝
echo.
echo 🎉 ShortURLPro宝塔面板部署准备工作已完成！
echo 📖 详细部署说明请参考：BT_PANEL_DEPLOYMENT.md
echo.
pause