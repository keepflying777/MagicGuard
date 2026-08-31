# MagicGuard 数据安全模块

MagicGuard 是一个数据安全模块（DSM），用于对业务系统提供数据脱敏与透明加密能力。

## 项目结构

```
MagicGuard/
├── magicguard-parent/          # 父项目
├── magicguard-service/         # 后端服务 (Spring Boot 3.x)
├── magicguard-admin/           # 管理控制台 (Vue 3 + Element Plus)
├── magicguard-client/          # 客户端 SDK
│   ├── client-core/            # 核心加解密逻辑
│   ├── windows/                # Windows 代理
│   └── linux/                  # Linux 代理
└── doc/                        # 文档
```

## 技术栈

| 组件 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.x + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 国密算法 | Bouncy Castle (SM4) |
| 管理前端 | Vue 3 + Element Plus |
| 客户端 SDK | Java + Bouncy Castle |
| 构建工具 | Maven + Vite |

## 功能模块

### 密钥管理

密钥用于可逆加密算法（如 FPE），支持 SM4 国密算法。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 密钥名称 | 密钥的显示名称 | `生产环境主密钥` |
| 密钥代码 | 唯一标识代码 | `KEY_PROD_001` |
| 算法 | 加密算法类型 | `SM4` |
| 密钥长度 | 密钥位长 | `128` |
| 密钥用途 | 密钥的使用场景 | `FIELD_ENCRYPT`（字段加密）/ `KEY_ENCRYPT`（密钥加密） |
| 状态 | 密钥状态 | `ACTIVE`（启用）/ `INACTIVE`（禁用） |
| 过期时间 | 密钥过期时间（可选） | `2025-12-31` |

#### 操作说明

1. **生成密钥**：点击「生成密钥」按钮，填写密钥名称、算法、长度和用途
2. **测试加解密**：选择密钥，输入测试数据，验证加解密结果
3. **密钥轮换**：对现有密钥进行轮换，生成新版本
4. **删除密钥**：删除未使用的密钥（正在使用的密钥无法删除）

---

### 脱敏规则

脱敏规则定义了对数据进行脱敏处理的方式。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 规则名称 | 规则的显示名称 | `手机号脱敏` |
| 规则代码 | 唯一标识代码 | `RULE_PHONE_MASK` |
| 算法类型 | 脱敏算法 | `MASK` / `REPLACE` / `TRUNCATE` / `FPE` / `HASH` |
| 参数配置 | 算法参数字典 | `{"prefixLen":3,"suffixLen":4}` |
| 优先级 | 执行优先级（数字越小越优先） | `100` |
| 状态 | 规则状态 | `1`（启用）/ `0`（禁用） |

#### 算法类型说明

| 算法 | 说明 | 参数示例 | 效果示例 |
|------|------|----------|----------|
| `MASK` | 掩码脱敏（不可逆） | `{"prefixLen":3,"suffixLen":4}` | `13812345678` → `138****5678` |
| `REPLACE` | 替换脱敏（不可逆） | `{"replaceChar":"*","replaceLen":4}` | `张三` → `张***` |
| `TRUNCATE` | 截断脱敏（不可逆） | `{"keepStart":2,"keepEnd":2}` | `13812345678` → `13****78` |
| `FPE` | 保形加密（可逆） | `{"key":"0123456789ABCDEF..."}` | `13812345678` → `加密字符串` |
| `HASH` | 哈希脱敏（不可逆） | `{"salt":"optional"}` | `13812345678` → `a1b2c3d4...` |

#### 参数配置说明

**MASK（掩码）算法：**
```json
{
  "prefixLen": 3,      // 保留前缀长度
  "suffixLen": 4,      // 保留后缀长度
  "maskChar": "*"      // 掩码字符（可选，默认*）
}
```

**REPLACE（替换）算法：**
```json
{
  "replaceChar": "*",  // 替换字符
  "replaceLen": 4      // 替换长度
}
```

**TRUNCATE（截断）算法：**
```json
{
  "keepStart": 2,      // 保留开头字符数
  "keepEnd": 2         // 保留结尾字符数
}
```

---

### 数据源

数据源配置用于连接业务数据库。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 数据源名称 | 显示名称 | `生产MySQL库` |
| 数据源代码 | 唯一标识代码 | `PROD_MYSQL_01` |
| 数据库类型 | 数据库类型 | `MYSQL` / `ORACLE` / `POSTGRESQL` / `SQLSERVER` / `DM` / `KINGBASE` |
| 主机地址 | 数据库地址 | `192.168.1.100` |
| 端口 | 数据库端口 | `3306` |
| 数据库名称 | 库名 | `magicguard` |
| 用户名 | 连接用户名 | `root` |
| 密码 | 连接密码 | `abc123..` |
| 分组 | 分组名称（可选） | `生产环境` |
| 环境标识 | 环境类型 | `PROD` / `TEST` / `DEV` |
| 描述 | 说明（可选） | `主数据库` |

#### 常用端口

| 数据库类型 | 默认端口 |
|------------|----------|
| MySQL | `3306` |
| Oracle | `1521` |
| PostgreSQL | `5432` |
| SQL Server | `1433` |
| 达梦 (DM) | `5236` |
| 人大金仓 (Kingbase) | `54321` |

#### 操作说明

1. **添加数据源**：填写数据库连接信息，点击「确认添加」
2. **测试连接**：保存前可点击「测试」验证连接是否正常
3. **编辑数据源**：修改数据库连接信息
4. **删除数据源**：删除未使用的数据源

---

### 脱敏任务

脱敏任务用于执行具体的数据脱敏操作。

#### 字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| 任务名称 | 任务显示名称 | `用户表手机号脱敏` |
| 源数据源 | 数据来源 | `PROD_MYSQL_01` |
| 源表名 | 要脱敏的表名 | `users`（多个用逗号分隔） |
| 目标类型 | 脱敏结果输出类型 | `DATABASE`（数据库）/ `FILE`（文件） |
| 目标数据源 | 异库脱敏时的目标库（可选） | `TEST_MYSQL_01` |
| 文件路径 | 目标类型为文件时的输出路径（可选） | `/var/exports/users.csv` |
| 执行类型 | 执行模式 | `FULL`（全量）/ `INCREMENT`（增量） |
| 调度类型 | 调度方式 | `IMMEDIATE`（立即）/ `SCHEDULED`（定时）/ `PERIODIC`（周期） |
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

1. **创建任务**：填写任务信息，配置脱敏规则
2. **执行任务**：任务创建后可立即执行或等待调度
3. **查看详情**：查看任务执行结果和日志
4. **重新执行**：对成功或失败的任务可重新执行
5. **编辑任务**：修改任务配置
6. **删除任务**：删除不需要的任务

---

## 快速开始

### 1. 初始化数据库

```sql
mysql -u root -p < magicguard-service/src/main/resources/schema.sql
```

### 2. 启动后端服务

```bash
cd magicguard-service
mvn spring-boot:run
```

服务将在 http://localhost:8080 启动。

### 3. 启动管理控制台

```bash
cd magicguard-admin
npm install
npm run dev
```

管理控制台将在 http://localhost:3000 启动。

### 4. 使用客户端 SDK

```java
import com.magicguard.client.MagicGuardClient;

public class Main {
    public static void main(String[] args) {
        MagicGuardClient client = new MagicGuardClient();

        // 加密数据
        String encrypted = client.encrypt("13812345678");

        // 解密数据
        String decrypted = client.decrypt(encrypted);

        System.out.println("加密: " + encrypted);
        System.out.println("解密: " + decrypted);
    }
}
```

## API 端点

### 密钥管理

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/keys` | GET | 获取所有密钥 |
| `/api/keys` | POST | 生成新密钥 |
| `/api/keys/generate` | POST | 生成密钥 |
| `/api/keys/{id}` | GET | 获取密钥详情 |
| `/api/keys/{id}/rotate` | POST | 密钥轮换 |
| `/api/keys/{id}` | DELETE | 删除密钥 |
| `/api/keys/encrypt` | POST | 加密数据 |
| `/api/keys/decrypt` | POST | 解密数据 |

### 脱敏规则

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/rules` | GET | 获取所有规则 |
| `/api/rules` | POST | 创建规则 |
| `/api/rules/{id}` | GET | 获取规则详情 |
| `/api/rules/{id}` | PUT | 更新规则 |
| `/api/rules/{id}` | DELETE | 删除规则 |
| `/api/rules/{id}/enabled` | PUT | 启用/禁用规则 |
| `/api/rules/test` | POST | 测试脱敏效果 |
| `/api/rules/algorithms` | GET | 获取支持的算法列表 |

### 数据源

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/datasources` | GET | 获取所有数据源 |
| `/api/datasources` | POST | 创建数据源 |
| `/api/datasources/{id}` | GET | 获取数据源详情 |
| `/api/datasources/{id}` | PUT | 更新数据源 |
| `/api/datasources/{id}` | DELETE | 删除数据源 |
| `/api/datasources/{id}/test` | POST | 测试连接 |
| `/api/datasources/{id}/tables` | GET | 获取数据源的所有表 |

### 脱敏任务

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/tasks` | GET | 获取所有任务 |
| `/api/tasks` | POST | 创建任务 |
| `/api/tasks/{id}` | GET | 获取任务详情 |
| `/api/tasks/{id}` | PUT | 更新任务 |
| `/api/tasks/{id}` | DELETE | 删除任务 |
| `/api/tasks/{id}/execute` | POST | 执行任务 |
| `/api/tasks/{id}/cancel` | POST | 取消任务 |

## 许可证

本项目为内部使用项目。
