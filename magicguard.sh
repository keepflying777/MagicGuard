#!/bin/bash

# MagicGuard 服务管理脚本
# 用法: ./magicguard.sh {start|stop|restart|status}

APP_DIR="/opt/MagicGuard"
LOG_DIR="/var/log/magicguard"
PID_DIR="/var/run/magicguard"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 创建必要目录
mkdir -p $LOG_DIR $PID_DIR

# 检查 MySQL
check_mysql() {
    if systemctl is-active --quiet mysqld; then
        log_info "MySQL is running"
        return 0
    else
        log_error "MySQL is not running"
        return 1
    fi
}

# 检查 Java
check_java() {
    if command -v java &> /dev/null; then
        version=$(java -version 2>&1 | head -1)
        log_info "Java: $version"
        return 0
    else
        log_error "Java is not installed"
        return 1
    fi
}

# 检查 Node.js
check_node() {
    if command -v node &> /dev/null; then
        version=$(node -v)
        log_info "Node.js: $version"
        return 0
    else
        log_error "Node.js is not installed"
        return 1
    fi
}

# 检查 Maven
check_maven() {
    if command -v mvn &> /dev/null; then
        version=$(mvn -version | head -1)
        log_info "Maven: $version"
        return 0
    else
        log_error "Maven is not installed"
        return 1
    fi
}

# 检查环境
check_env() {
    log_info "Checking environment..."

    local error=0

    check_java || error=1
    check_node || error=1
    check_maven || error=1
    check_mysql || error=1

    if [ $error -eq 1 ]; then
        log_error "Environment check failed"
        return 1
    fi

    log_info "Environment check passed"
    return 0
}

# 启动后端
start_backend() {
    log_info "Starting backend service..."

    if lsof -i :8080 &> /dev/null; then
        log_warn "Backend is already running on port 8080"
        return 0
    fi

    cd $APP_DIR/magicguard-service
    nohup mvn spring-boot:run > $LOG_DIR/backend.log 2>&1 &
    echo $! > $PID_DIR/backend.pid

    sleep 25

    if curl -s http://localhost:8080/api/keys &> /dev/null; then
        log_info "Backend started successfully (PID: $(cat $PID_DIR/backend.pid))"
    else
        log_error "Backend failed to start, check $LOG_DIR/backend.log"
        return 1
    fi
}

# 停止后端
stop_backend() {
    log_info "Stopping backend service..."

    if [ -f $PID_DIR/backend.pid ]; then
        pid=$(cat $PID_DIR/backend.pid)
        if ps -p $pid &> /dev/null; then
            kill $pid
            rm -f $PID_DIR/backend.pid
            log_info "Backend stopped"
        else
            log_warn "Backend process not found"
            rm -f $PID_DIR/backend.pid
        fi
    fi

    # 强制停止
    pkill -f "spring-boot:run" &> /dev/null
    pkill -f "magicguard-service" &> /dev/null
}

# 启动前端
start_frontend() {
    log_info "Starting frontend service..."

    if lsof -i :3000 &> /dev/null; then
        log_warn "Frontend is already running on port 3000"
        return 0
    fi

    cd $APP_DIR/magicguard-admin
    nohup npm run dev -- --host 0.0.0.0 --port 3000 > $LOG_DIR/frontend.log 2>&1 &
    echo $! > $PID_DIR/frontend.pid

    sleep 5

    if curl -s http://localhost:3000 &> /dev/null; then
        log_info "Frontend started successfully (PID: $(cat $PID_DIR/frontend.pid))"
    else
        log_error "Frontend failed to start, check $LOG_DIR/frontend.log"
        return 1
    fi
}

# 停止前端
stop_frontend() {
    log_info "Stopping frontend service..."

    if [ -f $PID_DIR/frontend.pid ]; then
        pid=$(cat $PID_DIR/frontend.pid)
        if ps -p $pid &> /dev/null; then
            kill $pid
            log_info "Frontend stopped"
        else
            log_warn "Frontend process not found"
        fi
        rm -f $PID_DIR/frontend.pid
    fi

    # 强制停止
    pkill -f "vite" &> /dev/null
    pkill -f "esbuild" &> /dev/null
}

# 查看状态
status() {
    log_info "MagicGuard Service Status"
    echo "================================"

    echo -n "MySQL: "
    if systemctl is-active --quiet mysqld; then
        echo -e "${GREEN}running${NC}"
    else
        echo -e "${RED}stopped${NC}"
    fi

    echo -n "Backend (8080): "
    if lsof -i :8080 &> /dev/null; then
        echo -e "${GREEN}running${NC} (PID: $(cat $PID_DIR/backend.pid 2>/dev/null))"
    else
        echo -e "${RED}stopped${NC}"
    fi

    echo -n "Frontend (3000): "
    if lsof -i :3000 &> /dev/null; then
        echo -e "${GREEN}running${NC} (PID: $(cat $PID_DIR/frontend.pid 2>/dev/null))"
    else
        echo -e "${RED}stopped${NC}"
    fi

    echo "================================"
}

# 启动所有服务
start() {
    if ! check_env; then
        exit 1
    fi

    start_backend
    start_frontend

    log_info "MagicGuard started successfully!"
    log_info "Frontend: http://localhost:3000"
    log_info "Backend: http://localhost:8080"
}

# 停止所有服务
stop() {
    log_info "Stopping MagicGuard..."
    stop_frontend
    stop_backend
    log_info "MagicGuard stopped"
}

# 主逻辑
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        sleep 2
        start
        ;;
    status)
        status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
