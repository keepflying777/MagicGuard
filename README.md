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

## 功能模块

### 密钥管理
- SM4/AES 密钥生成
- 密钥存储与轮换
- 字段级密钥绑定

### 脱敏规则
- MASK：掩码（如 138****5678）
- REPLACE：替换（如 张*）
- TRUNCATE：截断（如 a***@email.com）
- FPE：保形加密
- HASH：哈希

### 静态脱敏
- 库到库脱敏
- 库到文件脱敏
- 全量/增量脱敏

### 客户端代理
- Windows 代理
- Linux 代理
- 透明加解密

## API 端点

| 模块 | 端点 | 方法 |
|------|------|------|
| 密钥管理 | /api/keys/generate | POST |
| 密钥管理 | /api/keys | GET |
| 密钥管理 | /api/keys/{id}/rotate | POST |
| 脱敏规则 | /api/rules | GET/POST |
| 脱敏规则 | /api/rules/test | POST |
| 数据源 | /api/datasources | GET/POST |
| 脱敏任务 | /api/tasks | GET/POST |
| 脱敏任务 | /api/tasks/{id}/execute | POST |

## 许可证

本项目为内部使用项目。
