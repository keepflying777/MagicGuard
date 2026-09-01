#!/bin/bash

# MagicGuard 离线安装包构建脚本
# 用法: ./build-offline-package.sh
# 注意: 需要有网络连接来下载依赖包

set -e

echo "=========================================="
echo "  MagicGuard 离线安装包构建工具"
echo "=========================================="
echo ""

cd "$(dirname "$0")"

# 创建目录结构
PKG_DIR="MagicGuard-Offline-Package"
rm -rf "$PKG_DIR"
mkdir -p "$PKG_DIR"/{scripts,rpms,app}

echo "步骤1: 复制安装脚本..."
cp install.sh "$PKG_DIR/scripts/"
cp uninstall.sh "$PKG_DIR/scripts/"

echo "步骤2: 复制应用程序..."
# 复制父 pom.xml（magicguard-service 依赖它）
if [ -f "../pom.xml" ]; then
    cp ../pom.xml "$PKG_DIR/app/"
fi

if [ -d "../magicguard-service" ]; then
    cp -r ../magicguard-service "$PKG_DIR/app/"
fi

if [ -d "../magicguard-admin" ]; then
    cp -r ../magicguard-admin "$PKG_DIR/app/"
fi

if [ -f "../magicguard.sh" ]; then
    cp ../magicguard.sh "$PKG_DIR/app/"
fi

if [ -f "../README.md" ]; then
    cp ../README.md "$PKG_DIR/app/"
fi

if [ -f "../MagicGuard使用手册.md" ]; then
    cp "../MagicGuard使用手册.md" "$PKG_DIR/app/"
fi

# 复制 schema
if [ -f "../magicguard-service/src/main/resources/schema.sql" ]; then
    mkdir -p "$PKG_DIR/app/schema"
    cp ../magicguard-service/src/main/resources/schema.sql "$PKG_DIR/app/schema/"
fi

echo "步骤3: 下载 CentOS/Rocky 离线依赖包..."
mkdir -p "$PKG_DIR/rpms/centos"

# 检查系统类型
if command -v yum &>/dev/null || command -v dnf &>/dev/null; then
    echo "使用 dnf/yum 下载依赖（需要网络）..."

    # 下载 JDK 17
    echo "下载 JDK 17..."
    dnf download java-17-openjdk java-17-openjdk-devel --destdir="$PKG_DIR/rpms/centos/" -y 2>/dev/null || \
    echo "JDK 下载可能需要手动处理"

    # 下载 Maven
    echo "下载 Maven..."
    dnf download maven --destdir="$PKG_DIR/rpms/centos/" -y 2>/dev/null || \
    echo "Maven 下载可能需要手动处理"

    # 下载 Node.js 18
    echo "下载 Node.js 18..."
    dnf download nodejs --destdir="$PKG_DIR/rpms/centos/" -y 2>/dev/null || true

    # 下载 Git
    echo "下载 Git..."
    dnf download git --destdir="$PKG_DIR/rpms/centos/" -y 2>/dev/null || true

    # 下载 MySQL Server
    echo "下载 MySQL Server..."
    dnf download mysql-server --destdir="$PKG_DIR/rpms/centos/" -y 2>/dev/null || true
fi

echo "步骤4: 创建离线安装脚本..."

# 创建独立的离线安装脚本文件
cat > "$PKG_DIR/scripts/install-offline.sh" << 'INNER_EOF'
#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

APP_NAME="MagicGuard"
APP_DIR="/opt/MagicGuard"
LOG_DIR="/var/log/magicguard"
PID_DIR="/var/run/magicguard"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && cd .. && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   $APP_NAME 离线安装程序${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}错误: 请使用 root 用户运行此脚本${NC}"
    exit 1
fi

# 检测操作系统
detect_os() {
    if [ -f /etc/kylin-release ] || grep -qi "kylin" /etc/os-release 2>/dev/null; then
        echo "kylin"
    elif [ -f /etc/neokylin-release ] || grep -qi "neokylin" /etc/os-release 2>/dev/null; then
        echo "neokylin"
    elif [ -f /etc/redhat-release ]; then
        echo "centos"
    elif [ -f /etc/rocky-release ]; then
        echo "rocky"
    elif [ -f /etc/centos-release ]; then
        echo "centos"
    elif [ -f /etc/os-release ] && grep -qi "centos" /etc/os-release 2>/dev/null; then
        echo "centos"
    elif [ -f /etc/os-release ] && grep -qi "rocky" /etc/os-release 2>/dev/null; then
        echo "rocky"
    elif [ -f /etc/os-release ] && grep -qi "rhel" /etc/os-release 2>/dev/null; then
        echo "rhel"
    else
        echo "unknown"
    fi
}

# 辅助函数 - 检测依赖
check_dependency() {
    local cmd=$1
    local name=$2

    if command -v $cmd &>/dev/null; then
        version=$($cmd -version 2>&1 | head -1)
        echo -e "${GREEN}✓${NC} $name: $(echo $version | head -c 50)"
        return 0
    else
        echo -e "${RED}✗${NC} $name: 未安装"
        return 1
    fi
}

OS=$(detect_os)
echo -e "${GREEN}检测到操作系统: $OS${NC}"
echo ""

# 安装本地 RPM 包
install_local_rpms() {
    local rpms_dir="$SCRIPT_DIR/rpms/$OS"

    if [ ! -d "$rpms_dir" ]; then
        rpms_dir="$SCRIPT_DIR/rpms/centos"
    fi

    if [ ! -d "$rpms_dir" ]; then
        echo -e "${YELLOW}未找到本地 RPM 包目录: $rpms_dir${NC}"
        return 1
    fi

    echo -e "${YELLOW}从本地安装 RPM 包...${NC}"

    if ls "$rpms_dir"/*.rpm 1> /dev/null 2>&1; then
        if command -v yum &>/dev/null; then
            yum install -y "$rpms_dir"/*.rpm
        elif command -v dnf &>/dev/null; then
            dnf install -y "$rpms_dir"/*.rpm
        fi
        echo -e "${GREEN}RPM 包安装完成${NC}"
    else
        echo -e "${YELLOW}未找到 RPM 文件${NC}"
        return 1
    fi
}

# 检测并安装依赖
echo -e "${YELLOW}检查系统依赖...${NC}"
echo "----------------------------------------"

check_dependency java "JDK 17" || true
check_dependency mvn "Maven" || true
check_dependency node "Node.js" || true
check_dependency git "Git" || true

if ! systemctl is-active --quiet mysqld 2>/dev/null && ! systemctl is-active --quiet mysql 2>/dev/null; then
    echo -e "${RED}✗ MySQL: 未运行${NC}"
    echo ""
    echo "请选择安装方式:"
    echo "  1. 从本地 RPM 包安装（如果有）"
    echo "  2. 从网络安装"
    echo "  3. 跳过 MySQL 安装"
    read -p "请选择 (1/2/3): " choice

    case $choice in
        1)
            install_local_rpms
            ;;
        2)
            echo "从网络安装 MySQL..."
            if [ "$OS" == "centos" ] || [ "$OS" == "rocky" ] || [ "$OS" == "rhel" ] || [ "$OS" == "kylin" ] || [ "$OS" == "neokylin" ]; then
                if command -v dnf &>/dev/null; then
                    dnf module reset mysql -y 2>/dev/null || true
                    dnf module enable mysql:8.0 -y 2>/dev/null || true
                    dnf install -y mysql-server 2>/dev/null || dnf install -y mysql 2>/dev/null || true
                elif command -v yum &>/dev/null; then
                    yum install -y mysql-server 2>/dev/null || yum install -y mysql 2>/dev/null || true
                fi
                systemctl start mysqld 2>/dev/null || systemctl start mysql 2>/dev/null || true
                systemctl enable mysqld 2>/dev/null || systemctl enable mysql 2>/dev/null || true
            fi
            ;;
        3)
            echo -e "${YELLOW}跳过 MySQL 安装${NC}"
            ;;
    esac
else
    echo -e "${GREEN}✓ MySQL: 已运行${NC}"
fi

echo "----------------------------------------"
echo ""

# 安装应用程序
echo -e "${YELLOW}开始安装 $APP_NAME ...${NC}"
echo "----------------------------------------"

# 创建目录
mkdir -p $APP_DIR $LOG_DIR $PID_DIR

# 复制文件
echo "复制应用程序文件..."
cp -r "$SCRIPT_DIR/app/"* "$APP_DIR/"

# 设置权限
chmod +x "$APP_DIR/magicguard.sh" 2>/dev/null || true
chmod -R 755 "$APP_DIR" 2>/dev/null || true

# 创建符号链接
ln -sf "$APP_DIR/magicguard.sh" /usr/bin/magicguard

# 初始化数据库
echo "初始化数据库..."
read -p "是否初始化数据库? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS magicguard;" 2>/dev/null || true
    if [ -f "$APP_DIR/magicguard-service/src/main/resources/schema.sql" ]; then
        mysql -u root -p magicguard < "$APP_DIR/magicguard-service/src/main/resources/schema.sql" 2>/dev/null || true
    fi
    echo -e "${GREEN}数据库初始化完成${NC}"
fi

# 防火墙配置
echo "配置防火墙..."
if command -v firewall-cmd &>/dev/null; then
    firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true
    firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true
fi

echo ""
echo "----------------------------------------"
echo -e "${GREEN}安装完成!${NC}"
echo ""
echo "使用方法:"
echo -e "  启动服务:   ${YELLOW}magicguard start${NC}"
echo -e "  停止服务:   ${YELLOW}magicguard stop${NC}"
echo -e "  重启服务:   ${YELLOW}magicguard restart${NC}"
echo -e "  查看状态:   ${YELLOW}magicguard status${NC}"
echo ""
echo "访问地址:"
echo -e "  管理控制台: ${YELLOW}http://服务器IP:3000${NC}"
echo -e "  后端 API:   ${YELLOW}http://服务器IP:8080${NC}"
echo ""
echo -e "${BLUE}========================================${NC}"
INNER_EOF

chmod +x "$PKG_DIR/scripts/install-offline.sh"

echo "步骤5: 创建 README..."
cat > "$PKG_DIR/README.md" << 'EOF'
# MagicGuard 数据安全平台 - 离线安装包

## 简介

MagicGuard 是一款企业级数据安全平台，提供数据脱敏与透明加密能力。

## 离线安装说明

### 文件说明

```
MagicGuard-Offline-Package/
├── app/                    # 应用程序
│   ├── magicguard-service/ # 后端服务
│   ├── magicguard-admin/   # 前端控制台
│   ├── magicguard.sh      # 管理脚本
│   └── schema/            # 数据库脚本
├── rpms/                  # 离线依赖包
│   └── centos/           # CentOS/Rocky/Kylin 系统的 RPM 包
├── scripts/               # 安装脚本
│   ├── install.sh        # 在线安装脚本
│   ├── install-offline.sh # 离线安装脚本
│   └── uninstall.sh      # 卸载脚本
└── README.md             # 本文件
```

### 支持的操作系统

- CentOS 7/8
- Rocky Linux 8/9
- RHEL 7/8
- Kylin (麒麟)
- NeoKylin (中标麒麟)

### 安装步骤

#### 方式一：离线安装（推荐，无网络环境）

1. 解压缩安装包
```bash
tar -xzf MagicGuard-Offline-Package.tar.gz
cd MagicGuard-Offline-Package
```

2. 运行离线安装脚本
```bash
sudo ./scripts/install-offline.sh
```

3. 按照提示完成安装

#### 方式二：在线安装（有网络环境）

```bash
sudo ./scripts/install.sh
```

## 卸载

```bash
sudo ./scripts/uninstall.sh
```

## 注意事项

1. 离线安装包需要与操作系统版本匹配
2. 如果本地 RPM 包安装失败，可以选择在线安装
3. MySQL 需要提前配置 root 密码

## 文档

详细使用手册请参阅 `app/MagicGuard使用手册.md`
EOF

echo "步骤6: 打包..."
tar -czvf "${PKG_DIR}.tar.gz" "$PKG_DIR"

echo ""
echo "=========================================="
echo -e "${GREEN}离线安装包构建完成!${NC}"
echo "=========================================="
echo ""
echo "打包文件: ${PKG_DIR}.tar.gz"
echo ""
echo "下一步:"
echo "  1. 将 ${PKG_DIR}.tar.gz 拷贝到目标服务器"
echo "  2. 解压缩并运行 ./scripts/install-offline.sh"
echo ""
ls -lh "${PKG_DIR}.tar.gz"
