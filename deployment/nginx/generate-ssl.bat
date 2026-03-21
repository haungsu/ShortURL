@echo off
:: SSL证书生成脚本 - ShortURLPro (Windows版本)
:: 生成自签名SSL证书用于开发环境

setlocal enabledelayedexpansion

set DOMAIN=%1
if "%DOMAIN%"=="" set DOMAIN=shorturl.local

set SSL_DIR=nginx\ssl
set OPENSSL_CONF=C:\Program Files\OpenSSL-Win64\bin\openssl.cfg

echo === ShortURLPro SSL证书生成工具 ^(Windows^) ===
echo 域名: %DOMAIN%
echo 证书目录: %SSL_DIR%
echo.

:: 创建SSL目录
if not exist "%SSL_DIR%" mkdir "%SSL_DIR%"

:: 检查OpenSSL是否安装
where openssl >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  OpenSSL未找到，请先安装OpenSSL
    echo 下载地址: https://slproweb.com/products/Win32OpenSSL.html
    echo.
    pause
    exit /b 1
)

echo ✅ OpenSSL已安装
echo.

:: 生成私钥
echo 正在生成私钥...
openssl genrsa -out "%SSL_DIR%\privkey.pem" 2048
if %errorlevel% neq 0 (
    echo ❌ 私钥生成失败
    pause
    exit /b 1
)

:: 生成证书签名请求
echo 正在生成证书签名请求...
openssl req -new -key "%SSL_DIR%\privkey.pem" -out "%SSL_DIR%\cert.csr" -subj "/C=CN/ST=Beijing/L=Beijing/O=ShortURLPro/CN=%DOMAIN%"
if %errorlevel% neq 0 (
    echo ❌ 证书签名请求生成失败
    pause
    exit /b 1
)

:: 生成自签名证书
echo 正在生成自签名证书...
openssl x509 -req -days 365 -in "%SSL_DIR%\cert.csr" -signkey "%SSL_DIR%\privkey.pem" -out "%SSL_DIR%\fullchain.pem"
if %errorlevel% neq 0 (
    echo ❌ 证书生成失败
    pause
    exit /b 1
)

:: 清理临时文件
del "%SSL_DIR%\cert.csr" >nul 2>&1

:: 设置权限
attrib +R "%SSL_DIR%\privkey.pem"
attrib +R "%SSL_DIR%\fullchain.pem"

echo.
echo ✅ SSL证书生成完成！
echo 私钥: %SSL_DIR%\privkey.pem
echo 证书: %SSL_DIR%\fullchain.pem
echo.
echo ⚠️  注意：这是自签名证书，浏览器会提示不安全
echo 在生产环境中请使用Let's Encrypt等受信任的证书颁发机构
echo.

pause