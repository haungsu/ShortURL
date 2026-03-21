@echo off
REM ShortURLPro Windows重新部署脚本

echo === ShortURLPro 重新部署脚本 ===

REM 构建前端
echo 1. 构建前端应用...
cd ..\vue
call npm run build
if %errorlevel% neq 0 (
    echo ❌ 前端构建失败
    pause
    exit /b 1
)
echo ✅ 前端构建完成

REM 复制前端文件到Nginx目录
echo 2. 复制前端文件...
cd ..
xcopy /E /Y vue\dist\* deployment\nginx\html\

REM 重启Docker服务
echo 3. 重启Docker服务...
cd deployment\docker

REM 停止现有服务
docker-compose -f docker-compose.nginx.yml down

REM 启动服务
docker-compose -f docker-compose.nginx.yml up -d

if %errorlevel% equ 0 (
    echo ✅ 服务重启成功
    echo 访问地址: https://your-domain.com
) else (
    echo ❌ 服务启动失败
    echo 查看日志: docker-compose -f docker-compose.nginx.yml logs
)

echo === 部署完成 ===
pause