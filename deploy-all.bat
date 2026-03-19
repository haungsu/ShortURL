@echo off
setlocal

echo ========================================
echo ShortURLPro 性能优化一键部署脚本
echo ========================================

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Docker，请先安装Docker
    pause
    exit /b 1
)

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Java，请先安装Java 21
    pause
    exit /b 1
)

echo 1. 启动基础服务 (Redis + MySQL)...
docker-compose up -d redis mysql
if %errorlevel% neq 0 (
    echo 错误: 基础服务启动失败
    pause
    exit /b 1
)

timeout /t 10 /nobreak >nul
echo 基础服务启动完成

echo 2. 启动监控系统...
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
if %errorlevel% neq 0 (
    echo 错误: 监控系统启动失败
    pause
    exit /b 1
)

cd ..
echo 监控系统启动完成

echo 3. 编译应用...
call mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo 错误: 应用编译失败
    pause
    exit /b 1
)

echo 4. 启动应用服务...
start "ShortURL Application" cmd /c "mvn spring-boot:run && pause"

echo ========================================
echo 部署完成！
echo ========================================
echo 访问地址:
echo 应用服务: http://localhost:8080
echo Grafana: http://localhost:3000 (admin/admin123)
echo Prometheus: http://localhost:9090
echo Kibana: http://localhost:5601
echo ========================================
echo 按任意键退出...
pause >nul