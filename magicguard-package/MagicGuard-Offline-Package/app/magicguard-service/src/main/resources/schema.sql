-- MagicGuard 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS magicguard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE magicguard;

-- 加密密钥表
CREATE TABLE IF NOT EXISTS mg_encryption_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL COMMENT '密钥名称',
    key_code VARCHAR(64) NOT NULL UNIQUE COMMENT '密钥代码',
    algorithm VARCHAR(20) NOT NULL DEFAULT 'SM4' COMMENT '算法类型',
    key_length INT NOT NULL DEFAULT 128 COMMENT '密钥长度',
    encrypted_key TEXT NOT NULL COMMENT '加密后的密钥',
    iv VARCHAR(64) NOT NULL COMMENT 'IV向量',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ROTATED/REVOKED/DESTROYED',
    purpose VARCHAR(50) DEFAULT 'FIELD_ENCRYPT' COMMENT '用途',
    expire_time DATETIME COMMENT '失效时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(64) DEFAULT 'system',
    update_by VARCHAR(64),
    remark VARCHAR(500),
    INDEX idx_key_code (key_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密密钥表';

-- 字段密钥绑定表
CREATE TABLE IF NOT EXISTS mg_field_key_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    datasource_name VARCHAR(100) COMMENT '数据源名称',
    database_name VARCHAR(100) COMMENT '数据库名称',
    table_name VARCHAR(100) NOT NULL COMMENT '表名称',
    column_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    key_id BIGINT NOT NULL COMMENT '绑定的密钥ID',
    key_code VARCHAR(64) NOT NULL COMMENT '密钥代码',
    field_tag VARCHAR(50) COMMENT '字段标签: PII/FINANCIAL/MEDICAL/BUSINESS',
    encrypt_mode VARCHAR(20) DEFAULT 'CBC' COMMENT '加密模式',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    INDEX idx_table_column (table_name, column_name),
    INDEX idx_key_id (key_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段密钥绑定表';

-- 脱敏规则表
CREATE TABLE IF NOT EXISTS mg_mask_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(64) NOT NULL UNIQUE COMMENT '规则代码',
    algorithm_type VARCHAR(30) NOT NULL COMMENT '算法类型: MASK/REPLACE/TRUNCATE/FPE/HASH/RANDOM',
    algorithm_params TEXT COMMENT '算法参数JSON',
    target_type VARCHAR(20) DEFAULT 'COLUMN' COMMENT '目标类型: COLUMN/FILE',
    datasource_id BIGINT COMMENT '关联数据源ID',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    INDEX idx_rule_code (rule_code),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏规则表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS mg_datasource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    datasource_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    datasource_code VARCHAR(64) NOT NULL UNIQUE COMMENT '数据源代码',
    datasource_type VARCHAR(30) NOT NULL COMMENT '类型: MYSQL/ORACLE/POSTGRESQL/SQLSERVER/DM/KINGBASE',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(100) NOT NULL COMMENT '数据库名称',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    encrypted_password TEXT COMMENT '加密后的密码',
    connection_params TEXT COMMENT '连接参数JSON',
    group_name VARCHAR(100) COMMENT '分组名称',
    env_type VARCHAR(20) DEFAULT 'PROD' COMMENT '环境: PROD/TEST/DEV',
    description VARCHAR(500),
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    INDEX idx_datasource_code (datasource_code),
    INDEX idx_group (group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 脱敏任务表
CREATE TABLE IF NOT EXISTS mg_mask_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_code VARCHAR(64) NOT NULL UNIQUE COMMENT '任务代码',
    source_datasource_id BIGINT NOT NULL COMMENT '源数据源ID',
    target_datasource_id BIGINT COMMENT '目标数据源ID',
    target_type VARCHAR(20) DEFAULT 'DATABASE' COMMENT '目标类型: DATABASE/FILE',
    target_file_path VARCHAR(500) COMMENT '目标文件路径',
    source_tables VARCHAR(500) NOT NULL COMMENT '源表名',
    mask_rules_json TEXT NOT NULL COMMENT '脱敏规则JSON',
    exec_type VARCHAR(20) DEFAULT 'FULL' COMMENT '执行类型: FULL/INCREMENT',
    increment_column VARCHAR(100) COMMENT '增量字段',
    schedule_type VARCHAR(20) DEFAULT 'IMMEDIATE' COMMENT '调度类型: IMMEDIATE/SCHEDULED/PERIODIC',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    exec_time DATETIME COMMENT '执行时间',
    finish_time DATETIME COMMENT '完成时间',
    message TEXT COMMENT '执行信息',
    need_approval TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批',
    approval_status VARCHAR(20) COMMENT '审批状态',
    approver VARCHAR(64) COMMENT '审批人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(64) DEFAULT 'system',
    INDEX idx_task_code (task_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏任务表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS mg_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    operator VARCHAR(64) NOT NULL COMMENT '操作人',
    operator_ip VARCHAR(50) COMMENT '操作人IP',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id VARCHAR(100) COMMENT '目标ID',
    target_name VARCHAR(200) COMMENT '目标名称',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    status VARCHAR(20) NOT NULL COMMENT '状态: SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator (operator),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
