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

#### 功能说明

密钥用于可逆加密算法（如 FPE），支持 SM4 国密算法。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 密钥名称 | 密钥的显示名称 | 生产环境主密钥 |
| 密钥代码 | 唯一标识代码 | KEY_PROD_001 |
| 算法 | 加密算法类型 | SM4 |
| 密钥长度 | 密钥位长 | 128 |
| 密钥用途 | 密钥的使用场景 | FIELD_ENCRYPT（字段加密）/ KEY_ENCRYPT（密钥加密） |
| 状态 | 密钥状态 | ACTIVE（启用）/ INACTIVE（禁用） |
| 过期时间 | 密钥过期时间（可选） | 2025-12-31 |

#### 操作说明

1. **生成密钥**：点击「生成密钥」按钮，填写密钥名称、算法、长度和用途
2. **测试加解密**：选择密钥，输入测试数据，验证加解密结果
3. **密钥轮换**：对现有密钥进行轮换，生成新版本
4. **删除密钥**：删除未使用的密钥（正在使用的密钥无法删除）

---

### 2. 脱敏规则

#### 功能说明

脱敏规则定义了对数据进行脱敏处理的方式。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 规则名称 | 规则的显示名称 | 手机号脱敏 |
| 规则代码 | 唯一标识代码 | RULE_PHONE_MASK |
| 算法类型 | 脱敏算法 | MASK / REPLACE / TRUNCATE / FPE / HASH |
| 参数配置 | 算法参数字典 | {"prefixLen":3,"suffixLen":4} |
| 优先级 | 执行优先级（数字越小越优先） | 100 |
| 状态 | 规则状态 | 1（启用）/ 0（禁用） |

#### 算法类型说明

| 算法 | 说明 | 参数示例 | 效果示例 |
|------|------|----------|----------|
| MASK | 掩码脱敏（不可逆） | {"prefixLen":3,"suffixLen":4} | 13812345678 → 138****5678 |
| REPLACE | 替换脱敏（不可逆） | {"replaceChar":"*","replaceLen":4} | 张三 → 张*** |
| TRUNCATE | 截断脱敏（不可逆） | {"keepStart":2,"keepEnd":2} | 13812345678 → 13****78 |
| FPE | 保形加密（可逆） | {"key":"0123456789ABCDEF..."} | 13812345678 → 加密字符串 |
| HASH | 哈希脱敏（不可逆） | {"salt":"optional"} | 13812345678 → a1b2c3d4... |

#### 参数配置说明

**MASK（掩码）算法：**

```json
{
  "prefixLen": 3,
  "suffixLen": 4,
  "maskChar": "*"
}
```

- prefixLen：保留前缀长度
- suffixLen：保留后缀长度
- maskChar：掩码字符（可选，默认*）

**REPLACE（替换）算法：**

```json
{
  "replaceChar": "*",
  "replaceLen": 4
}
```

- replaceChar：替换字符
- replaceLen：替换长度

**TRUNCATE（截断）算法：**

```json
{
  "keepStart": 2,
  "keepEnd": 2
}
```

- keepStart：保留开头字符数
- keepEnd：保留结尾字符数

#### 操作说明

1. **创建规则**：点击「创建规则」按钮，选择算法类型并配置参数
2. **测试规则**：选择规则，输入测试数据，查看脱敏效果
3. **编辑规则**：修改规则配置
4. **启用/禁用**：通过开关快速启用或禁用规则
5. **删除规则**：删除未使用的规则

---

### 3. 数据源管理

#### 功能说明

数据源配置用于连接业务数据库。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 数据源名称 | 显示名称 | 生产MySQL库 |
| 数据源代码 | 唯一标识代码 | PROD_MYSQL_01 |
| 数据库类型 | 数据库类型 | MYSQL / ORACLE / POSTGRESQL / SQLSERVER / DM / KINGBASE |
| 主机地址 | 数据库地址 | 192.168.1.100 |
| 端口 | 数据库端口 | 3306 |
| 数据库名称 | 库名 | magicguard |
| 用户名 | 连接用户名 | root |
| 密码 | 连接密码 | abc123.. |
| 分组 | 分组名称（可选） | 生产环境 |
| 环境标识 | 环境类型 | PROD / TEST / DEV |
| 描述 | 说明（可选） | 主数据库 |

#### 常用端口

| 数据库类型 | 默认端口 |
|------------|----------|
| MySQL | 3306 |
| Oracle | 1521 |
| PostgreSQL | 5432 |
| SQL Server | 1433 |
| 达梦 (DM) | 5236 |
| 人大金仓 (Kingbase) | 54321 |

#### 操作说明

1. **添加数据源**：点击「添加数据源」按钮，填写数据库连接信息
2. **测试连接**：保存前可点击「测试」验证连接是否正常
3. **编辑数据源**：修改数据库连接信息
4. **删除数据源**：删除未使用的数据源

---

### 4. 脱敏任务

#### 功能说明

脱敏任务用于执行具体的数据脱敏操作。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 任务名称 | 任务显示名称 | 用户表手机号脱敏 |
| 源数据源 | 数据来源 | PROD_MYSQL_01 |
| 源表名 | 要脱敏的表名（多个用逗号分隔） | users |
| 目标类型 | 脱敏结果输出类型 | DATABASE（数据库）/ FILE（文件） |
| 目标数据源 | 异库脱敏时的目标库（可选） | TEST_MYSQL_01 |
| 文件路径 | 目标类型为文件时的输出路径（可选） | /var/exports/users.csv |
| 执行类型 | 执行模式 | FULL（全量）/ INCREMENT（增量） |
| 调度类型 | 调度方式 | IMMEDIATE（立即）/ SCHEDULED（定时）/ PERIODIC（周期） |
| 脱敏规则 | 脱敏规则配置 | 见下方说明 |

#### 脱敏规则配置格式

```json
[
  {
    "tableName": "users",
    "columns": [
      {"name": "phone", "ruleId": 1},
      {"name": "id_card", "ruleId": 2}
    ]
  },
  {
    "tableName": "orders",
    "columns": [
      {"name": "customer_phone", "ruleId": 1}
    ]
  }
]
```

| 字段 | 说明 |
|------|------|
| tableName | 表名 |
| columns | 字段脱敏配置数组 |
| columns[].name | 字段名 |
| columns[].ruleId | 脱敏规则ID（可在规则列表查看） |

#### 脱敏模式说明

| 模式 | 说明 |
|------|------|
| 同库脱敏 | 不选择目标数据源，直接更新源表数据 |
| 异库脱敏 | 选择目标数据源，将脱敏后数据写入目标库 |
| 文件脱敏 | 目标类型选择「文件」，导出CSV格式脱敏数据 |

#### 操作说明

1. **创建任务**：点击「创建任务」按钮，填写任务信息，配置脱敏规则
2. **执行任务**：任务创建后可点击「执行」按钮立即执行
3. **查看详情**：点击「详情」查看任务执行结果和日志
4. **重新执行**：对成功或失败的任务可点击「执行」重新运行
5. **编辑任务**：点击「编辑」修改任务配置
6. **删除任务**：点击「删除」移除不需要的任务

---

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

## API接口

### 密钥管理

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/keys | GET | 获取所有密钥 |
| /api/keys | POST | 生成新密钥 |
| /api/keys/generate | POST | 生成密钥 |
| /api/keys/{id} | GET | 获取密钥详情 |
| /api/keys/{id}/rotate | POST | 密钥轮换 |
| /api/keys/{id} | DELETE | 删除密钥 |
| /api/keys/encrypt | POST | 加密数据 |
| /api/keys/decrypt | POST | 解密数据 |

### 脱敏规则

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/rules | GET | 获取所有规则 |
| /api/rules | POST | 创建规则 |
| /api/rules/{id} | GET | 获取规则详情 |
| /api/rules/{id} | PUT | 更新规则 |
| /api/rules/{id} | DELETE | 删除规则 |
| /api/rules/{id}/enabled | PUT | 启用/禁用规则 |
| /api/rules/test | POST | 测试脱敏效果 |
| /api/rules/algorithms | GET | 获取支持的算法列表 |

### 数据源

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/datasources | GET | 获取所有数据源 |
| /api/datasources | POST | 创建数据源 |
| /api/datasources/{id} | GET | 获取数据源详情 |
| /api/datasources/{id} | PUT | 更新数据源 |
| /api/datasources/{id} | DELETE | 删除数据源 |
| /api/datasources/{id}/test | POST | 测试连接 |
| /api/datasources/{id}/tables | GET | 获取数据源的所有表 |

### 脱敏任务

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/tasks | GET | 获取所有任务 |
| /api/tasks | POST | 创建任务 |
| /api/tasks/{id} | GET | 获取任务详情 |
| /api/tasks/{id} | PUT | 更新任务 |
| /api/tasks/{id} | DELETE | 删除任务 |
| /api/tasks/{id}/execute | POST | 执行任务 |
| /api/tasks/{id}/cancel | POST | 取消任务 |

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
