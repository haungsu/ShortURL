@echo off
chcp 65001 >nul
title ShortURLPro 前端独立启动器
echo.
echo ========================================
echo   ShortURLPro 前端独立启动脚本
echo ========================================
echo.

REM 检查 Node.js
echo [检查] 正在检测 Node.js 环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js 20.19+ 或 22.12+
    echo 下载地址: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

REM 显示 Node.js 版本
for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i
echo [完成] Node.js 版本: %NODE_VERSION%

REM 检查 npm
echo [检查] 正在检测 npm...
npm --version >nul 2>&1
if errorlevel 1 (
    echo [错误] npm 不可用，请重新安装 Node.js
    pause
    exit /b 1
)

REM 显示 npm 版本
for /f "tokens=*" %%i in ('npm --version') do set NPM_VERSION=%%i
echo [完成] npm 版本: %NPM_VERSION%
echo.

REM 切换到前端目录
echo [准备] 切换到前端目录...
cd /d "%~dp0ShortURLPro\vue"
if errorlevel 1 (
    echo [错误] 无法进入前端目录，请确认项目结构
    echo 当前目录: %cd%
    pause
    exit /b 1
)

echo [完成] 当前工作目录: %cd%
echo.

REM 检查并安装依赖
echo [检查] 检查前端依赖...
if not exist "node_modules" (
    echo [安装] 未检测到依赖，正在安装...
    echo 这可能需要几分钟时间，请稍候...
    echo.
    
    REM 安装依赖
    call npm install
    if errorlevel 1 (
        echo.
        echo [错误] 依赖安装失败！
        echo 可能的解决方案:
        echo 1. 检查网络连接
        echo 2. 清除 npm 缓存: npm cache clean --force
        echo 3. 使用国内镜像源: npm config set registry https://registry.npmmirror.com
        echo.
        pause
        exit /b 1
    )
    
    echo.
    echo [完成] 依赖安装成功！
) else (
    echo [完成] 依赖已存在，跳过安装步骤
)

echo.
echo ========================================
echo   正在启动前端开发服务器...
echo ========================================
echo 访问地址: http://localhost:5173
echo 如端口被占用，Vite 会自动切换到其他端口
echo 按 Ctrl+C 可停止服务器
echo.

REM 启动开发服务器
npm run dev

REM 如果启动失败
if errorlevel 1 (
    echo.
    echo [错误] 前端启动失败！
    echo 请检查以下事项:
    echo 1. 端口是否被其他程序占用
    echo 2. 配置文件是否有语法错误
    echo 3. 依赖是否完整安装
    echo.
    pause
    exit /b 1
)

pause