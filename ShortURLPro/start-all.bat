@echo off
chcp 65001 >nul
title ShortURLPro 快捷启动

echo ================================
echo ShortURLPro 快捷启动菜单
echo ================================

:menu
echo.
echo 请选择要启动的服务:
echo 1. 启动前端开发服务器 (Vue)
echo 2. 构建并预览前端 (生产环境)
echo 3. 启动后端服务 (Spring Boot)
echo 4. 同时启动前后端
echo 5. 退出
echo.

set /p choice=请输入选项 (1-5): 

if "%choice%"=="1" (
    call "%~dp0start-frontend-dev.bat"
    goto menu
)
if "%choice%"=="2" (
    call "%~dp0build-frontend.bat"
    goto menu
)
if "%choice%"=="3" (
    echo 正在启动 Spring Boot 后端服务...
    cd /d "%~dp0"
    mvnw spring-boot:run
    goto menu
)
if "%choice%"=="4" (
    echo 同时启动前后端服务...
    echo 请分别运行前端和后端启动脚本
    echo 前端: start-frontend-dev.bat
    echo 后端: mvnw spring-boot:run
    pause
    goto menu
)
if "%choice%"=="5" (
    echo 再见！
    exit /b 0
)

echo 无效选项，请重新选择
goto menu