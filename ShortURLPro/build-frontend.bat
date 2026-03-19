@echo off
chcp 65001 >nul
title ShortURLPro 前端构建工具
echo.
echo ========================================
echo   ShortURLPro Vue 前端构建工具
echo ========================================
echo.

REM 检查 Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

REM 切换到 vue 目录
cd /d "%~dp0vue"
if errorlevel 1 (
    echo [错误] 无法进入 vue 目录
    pause
    exit /b 1
)

:menu
echo.
echo 请选择操作:
echo 1. 构建前端项目 (npm run build)
echo 2. 预览生产构建 (npm run preview)
echo 3. 构建并预览
echo 4. 返回上级菜单
echo.

set /p choice=请输入选项 (1-4): 

if "%choice%"=="1" goto build
if "%choice%"=="2" goto preview
if "%choice%"=="3" goto build_and_preview
if "%choice%"=="4" exit /b 0

echo 无效选项，请重新选择
goto menu

:build
echo.
echo ========================================
echo   正在构建前端项目...
echo ========================================
echo.

REM 检查 node_modules
if not exist "node_modules" (
    echo 未检测到 node_modules，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        goto menu
    )
)

call npm run build
if errorlevel 1 (
    echo.
    echo [错误] 构建失败
    pause
    goto menu
)

echo.
echo ========================================
echo   构建成功！
echo   输出目录: dist\
echo ========================================
echo.
pause
goto menu

:preview
echo.
echo ========================================
echo   正在启动预览服务器...
echo   访问地址: http://localhost:4173
echo ========================================
echo.

if not exist "dist\index.html" (
    echo [错误] 未找到构建文件，请先执行构建
    pause
    goto menu
)

call npm run preview
if errorlevel 1 (
    echo.
    echo [错误] 预览启动失败
    pause
    goto menu
)
goto menu

:build_and_preview
echo.
echo ========================================
echo   构建并预览模式
echo ========================================
echo.

REM 检查 node_modules
if not exist "node_modules" (
    echo 未检测到 node_modules，正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        goto menu
    )
)

call npm run build
if errorlevel 1 (
    echo.
    echo [错误] 构建失败
    pause
    goto menu
)

echo.
echo ========================================
echo   构建成功！正在启动预览服务器...
echo   访问地址: http://localhost:4173
echo ========================================
echo.

call npm run preview
if errorlevel 1 (
    echo.
    echo [错误] 预览启动失败
    pause
    goto menu
)
goto menu
