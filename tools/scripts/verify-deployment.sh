#!/bin/bash

# ShortURLPro 部署验证脚本
# 用于验证Vue Router History模式配置是否正确

echo "=== ShortURLPro 部署验证 ==="

# 检查必要文件是否存在
echo "1. 检查前端构建文件..."
if [ -f "/www/wwwroot/SortURLvue/dist/index.html" ]; then
    echo "✓ 前端构建文件存在"
else
    echo "✗ 前端构建文件不存在，请先构建Vue项目"
    echo "  运行: cd /path/to/vue && npm run build"
    exit 1
fi

# 检查Nginx配置语法
echo "2. 检查Nginx配置语法..."
nginx -t
if [ $? -eq 0 ]; then
    echo "✓ Nginx配置语法正确"
else
    echo "✗ Nginx配置有语法错误"
    exit 1
fi

# 重启Nginx
echo "3. 重启Nginx服务..."
systemctl restart nginx
if [ $? -eq 0 ]; then
    echo "✓ Nginx重启成功"
else
    echo "✗ Nginx重启失败"
    exit 1
fi

# 检查服务状态
echo "4. 检查服务运行状态..."
if systemctl is-active --quiet nginx; then
    echo "✓ Nginx服务运行中"
else
    echo "✗ Nginx服务未运行"
    exit 1
fi

# 验证关键路由
echo "5. 验证路由配置..."
echo "测试根路径..."
curl -s -o /dev/null -w "%{http_code}" http://localhost/ | grep -q "200" && echo "✓ 根路径访问正常" || echo "✗ 根路径访问异常"

echo "测试管理员路径..."
curl -s -o /dev/null -w "%{http_code}" http://localhost/admin | grep -q "200" && echo "✓ 管理员路径访问正常" || echo "✗ 管理员路径访问异常"

echo "测试API路径..."
curl -s -o /dev/null -w "%{http_code}" http://localhost/api/health | grep -q "200" && echo "✓ API路径访问正常" || echo "✗ API路径访问异常"

echo "6. 验证静态资源..."
if [ -d "/www/wwwroot/SortURLvue/dist/assets" ]; then
    echo "✓ 静态资源目录存在"
else
    echo "✗ 静态资源目录不存在"
fi

echo "=== 验证完成 ==="
echo "如果所有检查都通过，Vue Router History模式应该可以正常工作"
echo "现在可以直接访问 /admin 路径而不会出现 500 错误"