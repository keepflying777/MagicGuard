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
