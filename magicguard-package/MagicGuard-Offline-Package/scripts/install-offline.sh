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
    if [ -f /etc/redhat-release ]; then
        echo "centos"
    elif [ -f /etc/rocky-release ]; then
        echo "rocky"
    elif [ -f /etc/centos-release ]; then
        echo "centos"
    else
        echo "unknown"
    fi
}

OS=$(detect_os)
echo -e "${GREEN}检测到操作系统: $OS${NC}"
echo ""

# 检查网络
check_network() {
    if ping -c 1 8.8.8.8 &>/dev/null; then
        return 0
    else
        return 1
    fi
}

# 安装本地 RPM 包
install_local_rpms() {
    local rpms_dir="$SCRIPT_DIR/rpms/$OS"

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
            if [ "$OS" == "centos" ] || [ "$OS" == "rocky" ]; then
                dnf module reset mysql -y
                dnf module enable mysql:8.0 -y
                dnf install -y mysql-server
                systemctl start mysqld
                systemctl enable mysqld
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
echo "  启动服务:   ${YELLOW}magicguard start${NC}"
echo "  停止服务:   ${YELLOW}magicguard stop${NC}"
echo "  重启服务:   ${YELLOW}magicguard restart${NC}"
echo "  查看状态:   ${YELLOW}magicguard status${NC}"
echo ""
echo "访问地址:"
echo "  管理控制台: ${YELLOW}http://服务器IP:3000${NC}"
echo "  后端 API:   ${YELLOW}http://服务器IP:8080${NC}"
echo ""
echo -e "${BLUE}========================================${NC}"

# 辅助函数
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
