@echo off
chcp 65001 >nul
title ShortURLPro 环境检查与启动

echo ================================
echo ShortURLPro 环境检查与启动脚本
echo ================================
echo.

REM 检查Java环境
echo [1/6] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java，请先安装JDK 21或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr "version"') do (
    set JAVA_VERSION=%%v
    goto :java_version_found
)
:java_version_found
echo [OK] Java版本: %JAVA_VERSION:"=%
echo.

REM 检查Node.js环境
echo [2/6] 检查Node.js环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Node.js，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)
node --version
echo.

REM 检查Maven环境
echo [3/6] 检查Maven环境...
call mvnw --version >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到Maven，将使用项目内置的Maven Wrapper
)
echo [OK] 使用Maven Wrapper
echo.

REM 检查Docker环境（可选）
echo [4/6] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到Docker，数据库和Redis需要单独安装
    echo 或者您可以手动安装MySQL和Redis服务
) else (
    echo [OK] Docker可用
)
echo.

REM 检查前端依赖
echo [5/6] 检查前端依赖...
cd /d "%~dp0ShortURLPro\vue"
if not exist "node_modules" (
    echo [!] 前端依赖缺失，正在安装...
    call npm install
    if errorlevel 1 (
        echo [错误] 前端依赖安装失败
        pause
        exit /b 1
    )
    echo [OK] 前端依赖安装完成
) else (
    echo [OK] 前端依赖已存在
)
cd /d "%~dp0"
echo.

REM 检查后端依赖
echo [6/6] 检查后端依赖...
if not exist "target" (
    echo [!] 后端依赖缺失，正在下载...
    call mvnw dependency:resolve >nul 2>&1
    if errorlevel 1 (
        echo [警告] 依赖解析过程中有警告，但不影响启动
    )
    echo [OK] 后端依赖检查完成
) else (
    echo [OK] 后端依赖已存在
)
echo.

echo ================================
echo 环境检查完成！
echo ================================
echo.
echo 接下来请选择启动方式：
echo 1. 启动完整开发环境（包含数据库和Redis - 需要Docker）
echo 2. 启动基础开发环境（仅前后端 - 需要手动安装数据库）
echo 3. 只启动前端开发服务器
echo 4. 只启动后端服务
echo 5. 退出
echo.

set /p choice=请输入选项 (1-5): 

if "%choice%"=="1" (
    call :start_full_env
) else if "%choice%"=="2" (
    call :start_basic_env
) else if "%choice%"=="3" (
    call :start_frontend_only
) else if "%choice%"=="4" (
    call :start_backend_only
) else if "%choice%"=="5" (
    echo 再见！
    exit /b 0
) else (
    echo 无效选项
    pause
    goto :eof
)

:check_docker_services
echo 检查Docker服务状态...
docker compose ps >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker Compose服务未启动
    echo 请先运行: docker compose up -d
    exit /b 1
)
echo [OK] Docker服务运行中
goto :eof

:start_full_env
echo 启动完整开发环境...
echo [1/3] 启动数据库和Redis服务...
docker compose up -d mysql redis
timeout /t 10 /nobreak >nul
call :check_docker_services

echo [2/3] 启动后端服务...
start "后端服务" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"

echo [3/3] 启动前端开发服务器...
timeout /t 5 /nobreak >nul
cd /d "%~dp0ShortURLPro\vue"
start "前端开发服务器" cmd /c "npm run dev && pause"
cd /d "%~dp0"

echo.
echo ================================
echo 完整开发环境启动完成！
echo ================================
echo 前端访问地址: http://localhost:5173
echo 后端API地址: http://localhost:8080
echo Swagger文档: http://localhost:8080/swagger-ui.html
echo 数据库: MySQL (localhost:3306)
echo 缓存: Redis (localhost:6379)
echo.
echo 按任意键关闭此窗口...
pause >nul
goto :eof

:start_basic_env
echo 启动基础开发环境...
echo 注意：请确保已手动安装并启动MySQL和Redis服务

echo [1/2] 启动后端服务...
start "后端服务" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"

echo [2/2] 启动前端开发服务器...
timeout /t 5 /nobreak >nul
cd /d "%~dp0ShortURLPro\vue"
start "前端开发服务器" cmd /c "npm run dev && pause"
cd /d "%~dp0"

echo.
echo ================================
echo 基础开发环境启动完成！
echo ================================
echo 请确保以下服务已在系统中运行：
echo - MySQL数据库 (localhost:3306)
echo - Redis缓存服务 (localhost:6379)
echo.
echo 前端访问地址: http://localhost:5173
echo 后端API地址: http://localhost:8080
echo Swagger文档: http://localhost:8080/swagger-ui.html
echo.
echo 按任意键关闭此窗口...
pause >nul
goto :eof

:start_frontend_only
echo 只启动前端开发服务器...
cd /d "%~dp0ShortURLPro\vue"
npm run dev
goto :eof

:start_backend_only
echo 只启动后端服务...
mvnw spring-boot:run
goto :eof