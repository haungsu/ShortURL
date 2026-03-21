@echo off
REM ============================================
REM ShortURLPro 启动脚本 (Windows)
REM ============================================

echo ============================================
echo ShortURLPro 启动助手
echo ============================================

REM 检查Java环境
echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装Java 21+
    pause
    exit /b 1
)

REM 检查Node.js环境
echo 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js环境，请安装Node.js 20.19+或22.12+
    pause
    exit /b 1
)

REM 检查MySQL连接
echo 检查数据库连接...
mysql -u short_url_db -p123456 -e "SELECT 1" short_url_db >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: 无法连接到数据库，请确认：
    echo   - MySQL服务已启动
    echo   - 数据库 short_url_db 已创建
    echo   - 用户 short_url_db 密码 123456 已配置
    echo.
    echo 是否继续启动？(Y/N)
    set /p choice=
    if /i "%choice%" neq "Y" (
        echo 启动已取消
        pause
        exit /b 1
    )
)

REM 启动后端服务
echo 启动后端服务...
start "ShortURLPro Backend" cmd /k "cd /d %~dp0 && mvn spring-boot:run"

REM 等待后端启动
timeout /t 10 /nobreak >nul

REM 启动前端服务
echo 启动前端服务...
cd vue
start "ShortURLPro Frontend" cmd /k "npm run dev"

echo ============================================
echo 启动完成！
echo ============================================
echo 前端访问地址: http://localhost:5173
echo 后端API地址: http://localhost:8080
echo 管理员账号: admin / admin123
echo ============================================

pause