#!/bin/bash

# MagicGuard 安装包构建脚本
# 用法: ./build-package.sh

set -e

echo "开始构建 MagicGuard 安装包..."

cd "$(dirname "$0")"

# 创建临时目录
PKG_DIR="MagicGuard-Package"
rm -rf "$PKG_DIR"
mkdir -p "$PKG_DIR"

# 复制安装脚本
cp install.sh "$PKG_DIR/"
cp uninstall.sh "$PKG_DIR/"

# 复制应用程序
if [ -d "../magicguard-service" ]; then
    cp -r ../magicguard-service "$PKG_DIR/"
fi

if [ -d "../magicguard-admin" ]; then
    cp -r ../magicguard-admin "$PKG_DIR/"
fi

if [ -f "../magicguard.sh" ]; then
    cp ../magicguard.sh "$PKG_DIR/"
fi

# 复制许可证和文档
cp ../LICENSE "$PKG_DIR/" 2>/dev/null || true
cp ../README.md "$PKG_DIR/" 2>/dev/null || true

# 复制使用手册
cp ../MagicGuard使用手册.md "$PKG_DIR/" 2>/dev/null || true

# 复制数据库 schema
if [ -f "../magicguard-service/src/main/resources/schema.sql" ]; then
    mkdir -p "$PKG_DIR/schema"
    cp ../magicguard-service/src/main/resources/schema.sql "$PKG_DIR/schema/"
fi

# 创建 README
cat > "$PKG_DIR/README.md" << 'EOF'
# MagicGuard 数据安全平台

## 简介

MagicGuard 是一款企业级数据安全平台，提供数据脱敏与透明加密能力。

## 安装

### 前置要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 安装步骤

1. 解压缩安装包
```bash
tar -xzf MagicGuard-Package.tar.gz
cd MagicGuard-Package
```

2. 运行安装脚本
```bash
sudo ./install.sh
```

3. 按照提示完成安装

## 使用方法

```bash
magicguard start    # 启动服务
magicguard stop    # 停止服务
magicguard restart # 重启服务
magicguard status   # 查看状态
```

## 访问地址

- 管理控制台: http://服务器IP:3000
- 后端 API: http://服务器IP:8080

## 卸载

```bash
sudo ./uninstall.sh
```

## 文档

详细使用手册请参阅 `MagicGuard使用手册.md`
EOF

# 打包
echo "正在打包..."
tar -czf "${PKG_DIR}.tar.gz" "$PKG_DIR"

echo "构建完成: ${PKG_DIR}.tar.gz"
ls -lh "${PKG_DIR}.tar.gz"
