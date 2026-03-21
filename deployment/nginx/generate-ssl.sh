#!/bin/bash
# SSL证书生成脚本 - ShortURLPro
# 使用Let's Encrypt或自签名证书

set -e

DOMAIN=${1:-"shorturl.example.com"}
EMAIL=${2:-"admin@example.com"}
SSL_DIR="./nginx/ssl"

echo "=== ShortURLPro SSL证书生成工具 ==="
echo "域名: $DOMAIN"
echo "邮箱: $EMAIL"
echo "证书目录: $SSL_DIR"
echo

# 创建SSL目录
mkdir -p "$SSL_DIR"

# 检查是否已安装certbot
if ! command -v certbot &> /dev/null; then
    echo "⚠️  Certbot未安装，将生成自签名证书"
    echo "如需安装certbot，请运行:"
    echo "Ubuntu/Debian: sudo apt install certbot"
    echo "CentOS/RHEL: sudo yum install certbot"
    echo
    USE_SELF_SIGNED=true
else
    echo "✅ Certbot已安装"
    echo
    read -p "是否使用Let's Encrypt获取免费SSL证书？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        USE_LETSENCRYPT=true
    else
        USE_SELF_SIGNED=true
    fi
fi

if [[ $USE_LETSENCRYPT == true ]]; then
    echo "=== 使用Let's Encrypt获取证书 ==="
    
    # 检查80端口是否可用
    if lsof -Pi :80 -sTCP:LISTEN -t >/dev/null ; then
        echo "❌ 端口80已被占用，请先停止占用该端口的服务"
        exit 1
    fi
    
    # 获取证书
    sudo certbot certonly \
        --standalone \
        --preferred-challenges http \
        --email "$EMAIL" \
        --agree-tos \
        --no-eff-email \
        -d "$DOMAIN" \
        --config-dir "$SSL_DIR/letsencrypt" \
        --work-dir "$SSL_DIR/letsencrypt/work" \
        --logs-dir "$SSL_DIR/letsencrypt/logs"
    
    if [ $? -eq 0 ]; then
        echo "✅ Let's Encrypt证书获取成功"
        echo "证书路径: $SSL_DIR/letsencrypt/live/$DOMAIN/"
        
        # 创建符号链接到标准位置
        sudo ln -sf "$SSL_DIR/letsencrypt/live/$DOMAIN/fullchain.pem" "$SSL_DIR/fullchain.pem"
        sudo ln -sf "$SSL_DIR/letsencrypt/live/$DOMAIN/privkey.pem" "$SSL_DIR/privkey.pem"
        
        echo "✅ 证书链接已创建"
    else
        echo "❌ Let's Encrypt证书获取失败"
        exit 1
    fi
    
elif [[ $USE_SELF_SIGNED == true ]]; then
    echo "=== 生成自签名SSL证书 ==="
    
    # 生成私钥
    openssl genrsa -out "$SSL_DIR/privkey.pem" 2048
    
    # 生成证书签名请求
    openssl req -new -key "$SSL_DIR/privkey.pem" -out "$SSL_DIR/cert.csr" -subj "/C=CN/ST=Beijing/L=Beijing/O=ShortURLPro/CN=$DOMAIN"
    
    # 生成自签名证书（有效期365天）
    openssl x509 -req -days 365 -in "$SSL_DIR/cert.csr" -signkey "$SSL_DIR/privkey.pem" -out "$SSL_DIR/fullchain.pem"
    
    # 清理临时文件
    rm -f "$SSL_DIR/cert.csr"
    
    echo "✅ 自签名证书生成完成"
    echo "私钥: $SSL_DIR/privkey.pem"
    echo "证书: $SSL_DIR/fullchain.pem"
    echo "⚠️  注意：自签名证书浏览器会提示不安全，请在生产环境中使用Let's Encrypt"
fi

# 设置权限
chmod 600 "$SSL_DIR/privkey.pem"
chmod 644 "$SSL_DIR/fullchain.pem"

echo
echo "=== SSL证书配置完成 ==="
echo "请确保在nginx配置中正确引用证书路径"
echo "私钥路径: $SSL_DIR/privkey.pem"
echo "证书路径: $SSL_DIR/fullchain.pem"