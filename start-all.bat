@echo off
chcp 65001 >nul
title ShortURLPro 快捷启动

echo ================================
echo ShortURLPro 快捷启动菜单
echo ================================

:menu
echo.
echo 请选择要启动的服务:
echo 1. 环境检查与智能启动 (推荐)
echo 2. 启动前端开发服务器 (Vue)
echo 3. 构建并预览前端 (生产环境)
echo 4. 启动后端服务 (Spring Boot)
echo 5. 同时启动前后端
echo 6. 启动完整开发环境 (包含数据库)
echo 7. 退出
echo.

set /p choice=请输入选项 (1-7): 

if "%choice%"=="1" (
    call "%~dp0ensure-startup.bat"
    goto menu
)
if "%choice%"=="2" (
    call "%~dp0start-frontend-dev.bat"
    goto menu
)
if "%choice%"=="3" (
    echo 正在构建前端...
    cd /d "%~dp0ShortURLPro\vue"
    call npm run build
    if errorlevel 1 (
        echo 构建失败
        pause
        goto menu
    )
    call npm run preview
    goto menu
)
if "%choice%"=="4" (
    echo 正在启动 Spring Boot 后端服务...
    cd /d "%~dp0"
    call mvnw spring-boot:run
    goto menu
)
if "%choice%"=="5" (
    echo 同时启动前后端服务...
    echo 启动后端服务...
    start "后端服务" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"
    timeout /t 5 /nobreak >nul
    echo 启动前端开发服务器...
    cd /d "%~dp0ShortURLPro\vue"
    start "前端开发服务器" cmd /c "npm run dev && pause"
    cd /d "%~dp0"
    echo.
    echo 前后端服务已启动！
    echo 前端: http://localhost:5173
    echo 后端: http://localhost:8080
    pause
    goto menu
)
if "%choice%"=="6" (
    echo 启动完整开发环境...
    echo 启动数据库和Redis服务...
    docker compose up -d mysql redis
    timeout /t 10 /nobreak >nul
    echo 启动后端服务...
    start "后端服务" cmd /c "cd /d "%~dp0" && mvnw spring-boot:run && pause"
    timeout /t 5 /nobreak >nul
    echo 启动前端开发服务器...
    cd /d "%~dp0ShortURLPro\vue"
    start "前端开发服务器" cmd /c "npm run dev && pause"
    cd /d "%~dp0"
    echo.
    echo 完整开发环境已启动！
    echo 前端: http://localhost:5173
    echo 后端: http://localhost:8080
    echo 数据库: localhost:3306
    echo Redis: localhost:6379
    pause
    goto menu
)
if "%choice%"=="7" (
    echo 再见！
    exit /b 0
)

echo 无效选项，请重新选择
goto menu