#!/bin/bash

# MagicGuard 安装脚本
# 用法: ./install.sh

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

APP_NAME="MagicGuard"
APP_DIR="/opt/MagicGuard"
LOG_DIR="/var/log/magicguard"
PID_DIR="/var/run/magicguard"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   $APP_NAME 数据安全平台安装程序${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}错误: 请使用 root 用户运行此脚本${NC}"
    echo "或使用 sudo: sudo ./install.sh"
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
    elif [ -f /etc/debian_version ]; then
        echo "debian"
    elif [ -f /etc/ubuntu-release ]; then
        echo "ubuntu"
    else
        echo "unknown"
    fi
}

OS=$(detect_os)
echo -e "${GREEN}检测到操作系统: $OS${NC}"
echo ""

# 检查依赖
check_dependency() {
    local cmd=$1
    local name=$2
    local package=$3
    
    if command -v $cmd &> /dev/null; then
        version=$(eval $cmd -version 2>&1 | head -1)
        echo -e "${GREEN}✓${NC} $name: $(echo $version | head -c 50)"
        return 0
    else
        echo -e "${RED}✗${NC} $name: 未安装"
        return 1
    fi
}

echo -e "${YELLOW}检查系统依赖...${NC}"
echo "----------------------------------------"

MISSING_DEPS=()

# 检查 Java
if check_dependency "java" "JDK 17+" "java-17-openjdk java-17-openjdk-devel"; then
    JAVA_INSTALLED=true
else
    JAVA_INSTALLED=false
    MISSING_DEPS+=("java")
fi

# 检查 Maven
if check_dependency "mvn" "Maven 3.8+" "maven"; then
    MAVEN_INSTALLED=true
else
    MAVEN_INSTALLED=false
    MISSING_DEPS+=("maven")
fi

# 检查 Node.js
if check_dependency "node" "Node.js 18+" "nodejs"; then
    NODE_INSTALLED=true
else
    NODE_INSTALLED=false
    MISSING_DEPS+=("nodejs")
fi

# 检查 MySQL
if systemctl is-active --quiet mysqld 2>/dev/null || systemctl is-active --quiet mysql 2>/dev/null; then
    echo -e "${GREEN}✓${NC} MySQL: 已运行"
    MYSQL_INSTALLED=true
else
    echo -e "${RED}✗${NC} MySQL: 未运行"
    MYSQL_INSTALLED=false
    MISSING_DEPS+=("mysql-server")
fi

# 检查 Git
if check_dependency "git" "Git" "git"; then
    GIT_INSTALLED=true
else
    GIT_INSTALLED=false
    MISSING_DEPS+=("git")
fi

echo "----------------------------------------"
echo ""

# 安装缺失的依赖
install_dependencies() {
    if [ ${#MISSING_DEPS[@]} -eq 0 ]; then
        echo -e "${GREEN}所有依赖已满足，无需安装${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}检测到以下缺失的依赖:${NC}"
    for dep in "${MISSING_DEPS[@]}"; do
        echo "  - $dep"
    done
    echo ""
    
    read -p "是否自动安装缺失的依赖? (y/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}跳过依赖安装，请手动安装后重新运行此脚本${NC}"
        exit 0
    fi
    
    echo -e "${GREEN}开始安装依赖...${NC}"
    
    if [ "$OS" == "centos" ] || [ "$OS" == "rocky" ]; then
        # CentOS/Rocky Linux
        if [[ " ${MISSING_DEPS[*]} " =~ "java" ]]; then
            echo "安装 JDK 17..."
            dnf install -y java-17-openjdk java-17-openjdk-devel
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "maven" ]]; then
            echo "安装 Maven..."
            dnf install -y maven
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "nodejs" ]]; then
            echo "安装 Node.js 18..."
            curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
            dnf install -y nodejs
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "mysql-server" ]]; then
            echo "安装 MySQL Server..."
            dnf module reset mysql -y
            dnf module enable mysql:8.0 -y
            dnf install -y mysql-server
            systemctl start mysqld
            systemctl enable mysqld
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "git" ]]; then
            echo "安装 Git..."
            dnf install -y git
        fi
        
    elif [ "$OS" == "debian" ] || [ "$OS" == "ubuntu" ]; then
        # Debian/Ubuntu
        apt update
        
        if [[ " ${MISSING_DEPS[*]} " =~ "java" ]]; then
            echo "安装 JDK 17..."
            apt install -y openjdk-17-jdk
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "maven" ]]; then
            echo "安装 Maven..."
            apt install -y maven
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "nodejs" ]]; then
            echo "安装 Node.js 18..."
            curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
            apt install -y nodejs
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "mysql-server" ]]; then
            echo "安装 MySQL Server..."
            apt install -y mysql-server
            systemctl start mysql
            systemctl enable mysql
        fi
        
        if [[ " ${MISSING_DEPS[*]} " =~ "git" ]]; then
            echo "安装 Git..."
            apt install -y git
        fi
    else
        echo -e "${RED}不支持的操作系统，请手动安装依赖${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}依赖安装完成${NC}"
}

# 执行依赖安装
install_dependencies

echo ""
echo -e "${YELLOW}开始安装 $APP_NAME ...${NC}"
echo "----------------------------------------"

# 创建目录
echo "创建目录..."
mkdir -p $APP_DIR
mkdir -p $LOG_DIR
mkdir -p $PID_DIR

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 复制文件
echo "复制应用程序文件..."
if [ -d "$SCRIPT_DIR/magicguard-service" ]; then
    cp -r "$SCRIPT_DIR/magicguard-service" "$APP_DIR/"
fi

if [ -d "$SCRIPT_DIR/magicguard-admin" ]; then
    cp -r "$SCRIPT_DIR/magicguard-admin" "$APP_DIR/"
fi

if [ -f "$SCRIPT_DIR/magicguard.sh" ]; then
    cp "$SCRIPT_DIR/magicguard.sh" "$APP_DIR/"
    chmod +x "$APP_DIR/magicguard.sh"
fi

# 创建符号链接
echo "创建系统命令链接..."
ln -sf "$APP_DIR/magicguard.sh" /usr/bin/magicguard

# 设置权限
echo "设置文件权限..."
chmod +x "$APP_DIR/magicguard.sh" 2>/dev/null || true
chmod -R 755 "$APP_DIR/magicguard-service" 2>/dev/null || true
chmod -R 755 "$APP_DIR/magicguard-admin" 2>/dev/null || true

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
if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true
    firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true
elif command -v iptables &> /dev/null; then
    iptables -A INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null || true
    iptables -A INPUT -p tcp --dport 3000 -j ACCEPT 2>/dev/null || true
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
