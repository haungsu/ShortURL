@echo off
echo 正在构建 ShortURL Pro 前端项目...

:: 检查 Node.js 是否安装
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Node.js，请先安装 Node.js 20.19+ 或 22.12+
    pause
    exit /b 1
)

:: 检查 npm 是否可用
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 npm，请检查 Node.js 安装
    pause
    exit /b 1
)

:: 安装依赖（如果 node_modules 不存在）
if not exist "node_modules" (
    echo 正在安装依赖...
    npm install
    if %errorlevel% neq 0 (
        echo 依赖安装失败
        pause
        exit /b 1
    )
)

:: 清理旧的构建文件
if exist "dist" (
    echo 正在清理旧的构建文件...
    rmdir /s /q dist
)

:: 执行构建
echo 正在构建项目...
npm run build
if %errorlevel% neq 0 (
    echo 构建失败
    pause
    exit /b 1
)

echo 构建完成！文件位于 dist 目录中
pause