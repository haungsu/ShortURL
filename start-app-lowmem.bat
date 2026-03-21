@echo off
REM ============================================
REM ShortURLPro 低内存启动脚本 (Windows)
REM 适用于内存受限环境（<4GB）
REM ============================================

echo ============================================
echo ShortURLPro 低内存优化启动
echo ============================================
echo.

REM 检查 Java 环境
echo 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到 Java 环境，请安装 Java 21+
    pause
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%i
echo Java 版本：%JAVA_VERSION%
echo.

REM 低内存 JVM 配置（最大 512MB）
echo 应用低内存优化配置...
set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/heapdump.hprof -XX:SoftRefLRUPolicyMSPerMB=1000

echo JVM 内存配置：%JAVA_OPTS%
echo.

REM 检查数据库连接
echo 检查数据库连接...
mysql -u short_url_db -p123456 -e "SELECT 1" short_url_db >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告：无法连接到数据库，请确认：
    echo   - MySQL 服务已启动
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

REM 启动后端服务（低内存模式）
echo 启动后端服务（低内存模式）...
echo 预计内存占用：300-400MB
start "ShortURLPro Backend (Low Memory)" cmd /k "cd /d %~dp0 && set MAVEN_OPTS=%JAVA_OPTS% && mvn spring-boot:run"

echo 等待后端启动...
timeout /t 15 /nobreak >nul

REM 检查端口是否可用
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：后端服务启动失败，8080 端口未监听
    pause
    exit /b 1
)

echo 后端服务启动成功！
echo.

REM 启动前端服务
echo 启动前端服务...
cd vue
if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    call npm install
)
start "ShortURLPro Frontend" cmd /k "npm run dev"

echo ============================================
echo 启动完成！
echo ============================================
echo 前端访问地址：http://localhost:5173
echo 后端 API 地址：http://localhost:8080
echo 管理员账号：admin / admin123
echo ============================================
echo 内存优化模式：已启用
echo 预计总内存占用：400-500MB
echo ============================================
echo.

pause
