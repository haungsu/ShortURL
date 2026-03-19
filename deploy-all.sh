#!/bin/bash

echo "========================================"
echo "ShortURLPro 性能优化一键部署脚本"
echo "========================================"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未检测到Docker，请先安装Docker"
    exit 1
fi

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未检测到Java，请先安装Java 21"
    exit 1
fi

echo "1. 启动基础服务 (Redis + MySQL)..."
docker-compose up -d redis mysql
if [ $? -ne 0 ]; then
    echo "错误: 基础服务启动失败"
    exit 1
fi

sleep 10
echo "基础服务启动完成"

echo "2. 启动监控系统..."
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
if [ $? -ne 0 ]; then
    echo "错误: 监控系统启动失败"
    exit 1
fi

cd ..
echo "监控系统启动完成"

echo "3. 编译应用..."
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "错误: 应用编译失败"
    exit 1
fi

echo "4. 启动应用服务..."
mvn spring-boot:run &

echo "========================================"
echo "部署完成！"
echo "========================================"
echo "访问地址:"
echo "应用服务: http://localhost:8080"
echo "Grafana: http://localhost:3000 (admin/admin123)"
echo "Prometheus: http://localhost:9090"
echo "Kibana: http://localhost:5601"
echo "========================================"