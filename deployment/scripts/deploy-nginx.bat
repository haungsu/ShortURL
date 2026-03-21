@echo off
:: ShortURLPro Nginx部署快速启动脚本 (Windows)

setlocal enabledelayedexpansion

echo ========================================
echo ShortURLPro Nginx反向代理部署工具
echo ========================================
echo.

:MENU
echo 请选择部署模式:
echo 1. 开发环境部署 (HTTP only)
echo 2. 生产环境部署 (HTTPS with SSL)
echo 3. 仅生成SSL证书
echo 4. 启动服务
echo 5. 停止服务
echo 6. 查看服务状态
echo 7. 查看日志
echo 8. 退出
echo.

set /p choice=请输入选项 (1-8): 

if "%choice%"=="1" goto DEV_DEPLOY
if "%choice%"=="2" goto PROD_DEPLOY
if "%choice%"=="3" goto GENERATE_SSL
if "%choice%"=="4" goto START_SERVICES
if "%choice%"=="5" goto STOP_SERVICES
if "%choice%"=="6" goto SERVICE_STATUS
if "%choice%"=="7" goto VIEW_LOGS
if "%choice%"=="8" goto EXIT

echo 无效选项，请重新选择
echo.
goto MENU

:DEV_DEPLOY
echo.
echo === 开发环境部署 ===
echo.

:: 检查必要文件
if not exist "nginx\conf.d\shorturl-dev.conf" (
    echo ❌ 缺少开发环境配置文件
    pause
    goto MENU
)

:: 生成自签名证书
echo 正在生成SSL证书...
call nginx\generate-ssl.bat shorturl.local

:: 启动服务
echo 正在启动开发环境...
docker-compose -f docker-compose.dev.yml up -d

if %errorlevel% equ 0 (
    echo ✅ 开发环境启动成功
    echo 访问地址: http://localhost
) else (
    echo ❌ 启动失败，请检查错误信息
)

echo.
pause
goto MENU

:PROD_DEPLOY
echo.
echo === 生产环境部署 ===
echo.

set /p domain=请输入域名 (如: shorturl.yourdomain.com): 
if "%domain%"=="" (
    echo 域名不能为空
    pause
    goto PROD_DEPLOY
)

set /p email=请输入邮箱 (用于SSL证书): 
if "%email%"=="" (
    echo 邮箱不能为空
    pause
    goto PROD_DEPLOY
)

:: 生成SSL证书
echo 正在生成SSL证书...
if exist "C:\Program Files\Git\usr\bin\bash.exe" (
    "C:\Program Files\Git\usr\bin\bash.exe" nginx/generate-ssl.sh %domain% %email%
) else (
    echo ⚠️  未检测到Git Bash，请手动运行:
    echo cd nginx
    echo ./generate-ssl.sh %domain% %email%
    pause
    goto MENU
)

:: 启动服务
echo 正在启动生产环境...
docker-compose -f docker-compose.nginx.yml up -d

if %errorlevel% equ 0 (
    echo ✅ 生产环境启动成功
    echo 访问地址: https://%domain%
) else (
    echo ❌ 启动失败，请检查错误信息
)

echo.
pause
goto MENU

:GENERATE_SSL
echo.
echo === SSL证书生成 ===
echo.

set /p ssl_domain=请输入域名: 
if "%ssl_domain%"=="" set ssl_domain=shorturl.local

call nginx\generate-ssl.bat %ssl_domain%

echo.
pause
goto MENU

:START_SERVICES
echo.
echo === 启动服务 ===
echo.

echo 选择要启动的环境:
echo 1. 开发环境
echo 2. 生产环境
set /p env_choice=请选择 (1-2): 

if "%env_choice%"=="1" (
    docker-compose -f docker-compose.dev.yml up -d
    echo 开发环境启动完成
) else if "%env_choice%"=="2" (
    docker-compose -f docker-compose.nginx.yml up -d
    echo 生产环境启动完成
) else (
    echo 无效选择
)

echo.
pause
goto MENU

:STOP_SERVICES
echo.
echo === 停止服务 ===
echo.

echo 选择要停止的环境:
echo 1. 开发环境
echo 2. 生产环境
echo 3. 全部环境
set /p stop_choice=请选择 (1-3): 

if "%stop_choice%"=="1" (
    docker-compose -f docker-compose.dev.yml down
    echo 开发环境已停止
) else if "%stop_choice%"=="2" (
    docker-compose -f docker-compose.nginx.yml down
    echo 生产环境已停止
) else if "%stop_choice%"=="3" (
    docker-compose -f docker-compose.dev.yml down
    docker-compose -f docker-compose.nginx.yml down
    echo 所有环境已停止
) else (
    echo 无效选择
)

echo.
pause
goto MENU

:SERVICE_STATUS
echo.
echo === 服务状态 ===
echo.

echo 开发环境状态:
docker-compose -f docker-compose.dev.yml ps
echo.
echo 生产环境状态:
docker-compose -f docker-compose.nginx.yml ps

echo.
pause
goto MENU

:VIEW_LOGS
echo.
echo === 查看日志 ===
echo.

echo 选择要查看日志的服务:
echo 1. Nginx (开发环境)
echo 2. Nginx (生产环境)
echo 3. 应用服务
echo 4. 数据库
echo 5. Redis
set /p log_choice=请选择 (1-5): 

if "%log_choice%"=="1" (
    docker-compose -f docker-compose.dev.yml logs -f nginx-dev
) else if "%log_choice%"=="2" (
    docker-compose -f docker-compose.nginx.yml logs -f nginx
) else if "%log_choice%"=="3" (
    docker-compose -f docker-compose.nginx.yml logs -f short-url-app
) else if "%log_choice%"=="4" (
    docker-compose -f docker-compose.nginx.yml logs -f mysql
) else if "%log_choice%"=="5" (
    docker-compose -f docker-compose.nginx.yml logs -f redis
) else (
    echo 无效选择
)

echo.
pause
goto MENU

:EXIT
echo.
echo 感谢使用ShortURLPro部署工具！
echo.
exit /b 0