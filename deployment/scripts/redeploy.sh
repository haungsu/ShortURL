#!/bin/bash
# ShortURLPro 重新部署脚本

echo "=== ShortURLPro 重新部署脚本 ==="

# 构建前端
echo "1. 构建前端应用..."
cd ../vue
npm run build
if [ $? -ne 0 ]; then
    echo "❌ 前端构建失败"
    exit 1
fi
echo "✅ 前端构建完成"

# 复制前端文件到Nginx目录
echo "2. 复制前端文件..."
cd ..
cp -r vue/dist/* deployment/nginx/html/

# 重启Docker服务
echo "3. 重启Docker服务..."
cd deployment/docker

# 停止现有服务
docker-compose -f docker-compose.nginx.yml down

# 启动服务
docker-compose -f docker-compose.nginx.yml up -d

if [ $? -eq 0 ]; then
    echo "✅ 服务重启成功"
    echo "访问地址: https://your-domain.com"
else
    echo "❌ 服务启动失败"
    echo "查看日志: docker-compose -f docker-compose.nginx.yml logs"
fi

echo "=== 部署完成 ==="