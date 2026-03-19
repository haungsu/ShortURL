@echo off
chcp 65001 >nul
title ShortURLPro 前端开发服务器
echo.
echo ========================================
echo   ShortURLPro Vue 前端开发服务器启动
echo ========================================
echo.

REM 检查 Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo [1/3] Node.js 版本: 
node --version
echo.

REM 切换到 vue 目录
cd /d "%~dp0vue"
if errorlevel 1 (
    echo [错误] 无法进入 vue 目录
    pause
    exit /b 1
)

echo [2/3] 当前目录: %cd%
echo.

REM 检查 node_modules
if not exist "node_modules" (
    echo [3/3] 未检测到 node_modules，正在安装依赖...
    echo 这可能需要几分钟，请耐心等待...
    echo.
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
    echo.
    echo 依赖安装完成！
) else (
    echo [3/3] 依赖已安装，跳过安装步骤
)

echo.
echo ========================================
echo   正在启动 Vite 开发服务器...
echo   访问地址: http://localhost:5173
echo ========================================
echo.

npm run dev

if errorlevel 1 (
    echo.
    echo [错误] 启动失败，请检查错误信息
    pause
    exit /b 1
)

pause
