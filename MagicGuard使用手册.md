# MagicGuard 数据安全平台使用手册

## 目录

1. [系统简介](#系统简介)
2. [系统要求](#系统要求)
3. [安装部署](#安装部署)
4. [快速开始](#快速开始)
5. [功能模块](#功能模块)
6. [使用指南](#使用指南)
7. [API接口](#api接口)
8. [常见问题](#常见问题)

---

## 系统简介

MagicGuard 是一款企业级数据安全平台，提供数据脱敏与透明加密能力。系统采用前后端分离架构，支持多种国密算法和数据脱敏策略。

**核心能力：**
- 密钥生命周期管理（生成、轮换、销毁）
- 多种脱敏算法（MASK/REPLACE/TRUNCATE/FPE/HASH）
- 支持 SM4/AES 国密算法
- 静态脱敏任务调度
- U-Key 双因素认证（可选）

---

## 系统要求

### 硬件要求

| 项目 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 磁盘 | 20 GB | 50 GB |

### 软件要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| 操作系统 | Linux (CentOS/Rocky/Ubuntu) |

---

## 安装部署

### 方式一：RPM 包安装（推荐）

```bash
# CentOS/Rocky Linux
sudo dnf install -y java-17-openjdk java-17-openjdk-devel maven mysql-server nodejs git

# Ubuntu/Debian
sudo apt install -y openjdk-17-jdk maven mysql-server nodejs git
```

### 方式二：手动安装

#### 1. 安装 JDK 17

```bash
# CentOS/Rocky
sudo dnf install -y java-17-openjdk java-17-openjdk-devel

# 验证安装
java -version
```

#### 2. 安装 Maven

```bash
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz -C /opt
sudo ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/bin/mvn
mvn -version
```

#### 3. 安装 MySQL 8.0

```bash
# CentOS/Rocky
sudo dnf module reset mysql -y
sudo dnf module enable mysql:8.0 -y
sudo dnf install -y mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 设置 root 密码
mysql -u root -p
```

#### 4. 安装 Node.js 18

```bash
# CentOS/Rocky
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo dnf install -y nodejs

# 验证安装
node -v
npm -v
```

### 部署步骤

#### 1. 克隆代码

```bash
git clone https://github.com/keepflying777/MagicGuard.git /opt/MagicGuard
cd /opt/MagicGuard
```

#### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS magicguard;"

# 导入表结构
mysql -u root -p magicguard < magicguard-service/src/main/resources/schema.sql
```

#### 3. 配置数据库连接

编辑 `magicguard-service/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/magicguard
    username: root
    password: YourPassword
```

#### 4. 构建后端服务

```bash
cd magicguard-service
mvn clean package -DskipTests
```

#### 5. 构建前端控制台

```bash
cd magicguard-admin
npm install
npm run build
```

#### 6. 使用 Nginx 部署

```bash
# 安装 Nginx
sudo dnf install -y nginx

# 复制前端构建产物
sudo rm -rf /usr/share/nginx/html/*
sudo cp -r /opt/MagicGuard/magicguard-admin/dist/* /usr/share/nginx/html/

# 配置 Nginx 反向代理
sudo vi /etc/nginx/nginx.conf

# 在 http 段添加：
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /usr/share/nginx/html;
        index index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 启动服务
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 7. 启动后端服务

```bash
cd /opt/MagicGuard/magicguard-service
mvn spring-boot:run &
```

#### 8. 启动前端控制台

```bash
cd /opt/MagicGuard/magicguard-admin
npm run dev -- --host 0.0.0.0 --port 3000 &
```

---

## 快速开始

### 服务启动（服务器重启后）

```bash
# 使用管理脚本（推荐）
cd /opt/MagicGuard
./magicguard.sh restart

# 或手动启动
# 1. 启动 MySQL
systemctl start mysqld

# 2. 启动后端
cd /opt/MagicGuard/magicguard-service
mvn spring-boot:run &

# 3. 启动前端
cd /opt/MagicGuard/magicguard-admin
npm run dev -- --host 0.0.0.0 --port 3000 &
```

### 服务管理命令

```bash
./magicguard.sh start    # 启动所有服务
./magicguard.sh stop     # 停止所有服务
./magicguard.sh restart  # 重启所有服务
./magicguard.sh status   # 查看服务状态
```

### 默认账号

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| operator | operator123 | 普通用户 |

### 访问地址

- **管理控制台**：http://服务器IP/
- **后端 API**：http://服务器IP/api

---

## 功能模块

### 1. 密钥管理

- **生成密钥**：支持 SM4/AES 算法，128/256 位密钥长度
- **密钥轮换**：对旧密钥进行轮换，新密钥替代旧密钥
- **密钥销毁**：安全销毁密钥，数据将无法解密
- **加解密测试**：在线测试加解密功能

### 2. 脱敏规则

支持以下脱敏算法：

| 算法 | 说明 | 示例 |
|------|------|------|
| MASK | 掩码脱敏 | 13812345678 → 138****5678 |
| REPLACE | 替换脱敏 | 张三 → 张* |
| TRUNCATE | 截断脱敏 | a***@email.com |
| FPE | 保形加密 | 保持格式的加密 |
| HASH | 哈希脱敏 | 单向不可逆 |

### 3. 数据源管理

支持多种数据库类型：
- MySQL
- Oracle
- PostgreSQL
- SQL Server
- DM（达梦）
- Kingbase（人大金仓）

### 4. 脱敏任务

- **任务类型**：全量脱敏 / 增量脱敏
- **调度方式**：立即执行 / 定时执行 / 周期执行
- **任务审批**：支持审批流程

### 5. 日志管理

- 操作日志记录
- 筛选：按状态、按模块
- 日志详情查看

### 6. 用户管理

- 用户列表
- 添加/编辑用户
- 角色管理（管理员/普通用户）
- 密码重置

### 7. U-Key 管理（可选）

- U-Key 设备绑定
- 绑定用户
- 设备状态管理
- U-Key 登录支持双因素认证

---

## 使用指南

### 1. 首次配置

1. 使用管理员账号登录
2. 进入「数据源管理」，添加业务数据库连接
3. 进入「密钥管理」，生成主密钥
4. 进入「脱敏规则」，创建脱敏规则

### 2. 创建脱敏任务

1. 进入「脱敏任务」
2. 点击「创建任务」
3. 选择源数据源和目标
4. 配置脱敏规则
5. 选择执行类型和调度方式
6. 提交任务

### 3. 执行脱敏

1. 在任务列表找到待执行任务
2. 点击「执行」
3. 确认后任务开始运行
4. 可在详情中查看执行进度

---

## API接口

### 密钥管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/keys` | GET | 获取密钥列表 |
| `/api/keys/generate` | POST | 生成新密钥 |
| `/api/keys/{id}/rotate` | POST | 密钥轮换 |
| `/api/keys/{id}` | DELETE | 删除密钥 |

### 脱敏规则

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/rules` | GET | 获取规则列表 |
| `/api/rules` | POST | 创建规则 |
| `/api/rules/test` | POST | 测试规则 |

### 数据源

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/datasources` | GET | 获取数据源列表 |
| `/api/datasources` | POST | 添加数据源 |
| `/api/datasources/{id}/test` | POST | 测试连接 |

### 脱敏任务

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/tasks` | GET | 获取任务列表 |
| `/api/tasks` | POST | 创建任务 |
| `/api/tasks/{id}/execute` | POST | 执行任务 |

---

## 常见问题

### Q1: 后端启动失败？

**检查项：**
- JDK 版本是否为 17+
- MySQL 是否运行中
- 数据库连接配置是否正确
- 端口 8080 是否被占用

### Q2: 前端无法访问？

**检查项：**
- Nginx 是否运行
- 防火墙是否开放 80 端口
- 前端构建是否成功

### Q3: 数据库连接失败？

**检查项：**
- MySQL 服务是否启动
- 用户名密码是否正确
- 数据库是否已创建

### Q4: 密钥加密失败？

**检查项：**
- 主密钥是否配置
- 主密钥格式是否为 32 位

---

## 生产环境建议

1. **安全加固**
   - 修改默认密码
   - 配置 HTTPS
   - 启用防火墙
   - 定期备份数据库

2. **性能优化**
   - 使用 Redis 缓存（可选）
   - 配置连接池
   - 使用 Nginx 动静分离

3. **高可用部署**
   - 后端多实例部署
   - MySQL 主从复制
   - Nginx 负载均衡

---

## 版本信息

- 当前版本：v5.1.0
- 发布日期：2026-08-20
- GitHub：https://github.com/keepflying777/MagicGuard

---

## 技术支持

如有问题，请联系技术支持或提交 Issue。
