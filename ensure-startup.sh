#!/bin/bash

# ShortURLPro 环境检查与启动脚本
echo "================================"
echo "ShortURLPro 环境检查与启动脚本"
echo "================================"
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}[错误]${NC} 未检测到 $1，请先安装"
        return 1
    fi
    return 0
}

# 显示成功消息
success_msg() {
    echo -e "${GREEN}[√]${NC} $1"
}

# 显示警告消息
warning_msg() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# 显示错误消息
error_msg() {
    echo -e "${RED}[错误]${NC} $1"
}

# 检查Java环境
echo "[1/6] 检查Java环境..."
if ! check_command java; then
    error_msg "请先安装JDK 21或更高版本"
    echo "下载地址: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
success_msg "Java版本: $JAVA_VERSION"
echo

# 检查Node.js环境
echo "[2/6] 检查Node.js环境..."
if ! check_command node; then
    error_msg "请先安装Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node --version)
success_msg "Node.js版本: $NODE_VERSION"
echo

# 检查Maven环境
echo "[3/6] 检查Maven环境..."
if command -v ./mvnw &> /dev/null; then
    success_msg "使用Maven Wrapper"
else
    warning_msg "未找到Maven Wrapper，将使用系统Maven"
    if ! check_command mvn; then
        error_msg "请安装Maven或确保mvnw可执行"
        exit 1
    fi
fi
echo

# 检查Docker环境
echo "[4/6] 检查Docker环境..."
if check_command docker; then
    success_msg "Docker可用"
else
    warning_msg "未检测到Docker，数据库和Redis需要单独安装"
    echo "或者您可以手动安装MySQL和Redis服务"
fi
echo

# 检查前端依赖
echo "[5/6] 检查前端依赖..."
cd "$(dirname "$0")/ShortURLPro/vue" || exit 1
if [ ! -d "node_modules" ]; then
    warning_msg "前端依赖缺失，正在安装..."
    npm install
    if [ $? -ne 0 ]; then
        error_msg "前端依赖安装失败"
        exit 1
    fi
    success_msg "前端依赖安装完成"
else
    success_msg "前端依赖已存在"
fi
cd - > /dev/null || exit 1
echo

# 检查后端依赖
echo "[6/6] 检查后端依赖..."
if [ ! -d "target" ]; then
    warning_msg "后端依赖缺失，正在解析..."
    ./mvnw dependency:resolve > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        warning_msg "依赖解析过程中有警告，但不影响启动"
    fi
    success_msg "后端依赖检查完成"
else
    success_msg "后端依赖已存在"
fi
echo

echo "================================"
echo "环境检查完成！"
echo "================================"
echo
echo "接下来请选择启动方式："
echo "1. 启动完整开发环境（包含数据库和Redis - 需要Docker）"
echo "2. 启动基础开发环境（仅前后端 - 需要手动安装数据库）"
echo "3. 只启动前端开发服务器"
echo "4. 只启动后端服务"
echo "5. 退出"
echo

read -p "请输入选项 (1-5): " choice

check_docker_services() {
    echo "检查Docker服务状态..."
    if ! docker compose ps > /dev/null 2>&1; then
        error_msg "Docker Compose服务未启动"
        echo "请先运行: docker compose up -d"
        exit 1
    fi
    success_msg "Docker服务运行中"
}

start_full_env() {
    echo "启动完整开发环境..."
    echo "[1/3] 启动数据库和Redis服务..."
    docker compose up -d mysql redis
    sleep 10
    check_docker_services

    echo "[2/3] 启动后端服务..."
    gnome-terminal --title="后端服务" -- bash -c "cd '$(pwd)' && ./mvnw spring-boot:run; read -p '按回车键继续...'"

    echo "[3/3] 启动前端开发服务器..."
    sleep 5
    cd "$(dirname "$0")/ShortURLPro/vue" || exit 1
    gnome-terminal --title="前端开发服务器" -- bash -c "npm run dev; read -p '按回车键继续...'"
    cd - > /dev/null || exit 1

    echo
    echo "================================"
    echo "完整开发环境启动完成！"
    echo "================================"
    echo "前端访问地址: http://localhost:5173"
    echo "后端API地址: http://localhost:8080"
    echo "Swagger文档: http://localhost:8080/swagger-ui.html"
    echo "数据库: MySQL (localhost:3306)"
    echo "缓存: Redis (localhost:6379)"
    echo
}

start_basic_env() {
    echo "启动基础开发环境..."
    echo "注意：请确保已手动安装并启动MySQL和Redis服务"

    echo "[1/2] 启动后端服务..."
    gnome-terminal --title="后端服务" -- bash -c "cd '$(pwd)' && ./mvnw spring-boot:run; read -p '按回车键继续...'"

    echo "[2/2] 启动前端开发服务器..."
    sleep 5
    cd "$(dirname "$0")/ShortURLPro/vue" || exit 1
    gnome-terminal --title="前端开发服务器" -- bash -c "npm run dev; read -p '按回车键继续...'"
    cd - > /dev/null || exit 1

    echo
    echo "================================"
    echo "基础开发环境启动完成！"
    echo "================================"
    echo "请确保以下服务已在系统中运行："
    echo "- MySQL数据库 (localhost:3306)"
    echo "- Redis缓存服务 (localhost:6379)"
    echo
    echo "前端访问地址: http://localhost:5173"
    echo "后端API地址: http://localhost:8080"
    echo "Swagger文档: http://localhost:8080/swagger-ui.html"
    echo
}

start_frontend_only() {
    echo "只启动前端开发服务器..."
    cd "$(dirname "$0")/ShortURLPro/vue" || exit 1
    npm run dev
}

start_backend_only() {
    echo "只启动后端服务..."
    ./mvnw spring-boot:run
}

case $choice in
    1) start_full_env ;;
    2) start_basic_env ;;
    3) start_frontend_only ;;
    4) start_backend_only ;;
    5) echo "再见！"; exit 0 ;;
    *) echo "无效选项"; exit 1 ;;
esac

echo "按任意键关闭此窗口..."
read -n 1 -s