package com.magicguard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段密钥绑定实体 - 一字段一密钥
 */
@Data
@TableName("mg_field_key_binding")
public class FieldKeyBinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 绑定的密钥 ID
     */
    private Long keyId;

    /**
     * 密钥代码
     */
    private String keyCode;

    /**
     * 字段标签：PII, FINANCIAL, MEDICAL, BUSINESS
     */
    private String fieldTag;

    /**
     * 加密模式：ECB, CBC
     */
    private String encryptMode;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
