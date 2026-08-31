#!/bin/bash

# MagicGuard 卸载脚本
# 用法: ./uninstall.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   MagicGuard 卸载程序${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}错误: 请使用 root 用户运行此脚本${NC}"
    exit 1
fi

read -p "确定要卸载 MagicGuard 吗? (y/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "取消卸载"
    exit 0
fi

echo -e "${YELLOW}开始卸载...${NC}"

# 停止服务
echo "停止服务..."
if [ -f /opt/MagicGuard/magicguard.sh ]; then
    /opt/MagicGuard/magicguard.sh stop 2>/dev/null || true
fi

# 删除系统命令链接
echo "删除系统命令链接..."
rm -f /usr/bin/magicguard 2>/dev/null || true

# 删除应用目录
echo "删除应用目录..."
rm -rf /opt/MagicGuard 2>/dev/null || true

# 删除日志目录（可选）
read -p "是否删除日志和数据? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf /var/log/magicguard 2>/dev/null || true
    rm -rf /var/run/magicguard 2>/dev/null || true
    read -p "是否删除数据库? 警告: 数据库将被完全删除! (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        mysql -u root -p -e "DROP DATABASE IF EXISTS magicguard;" 2>/dev/null || true
        echo -e "${GREEN}数据库已删除${NC}"
    fi
fi

echo ""
echo -e "${GREEN}卸载完成!${NC}"
echo "感谢使用 MagicGuard"
