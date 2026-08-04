package com.magicguard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置实体
 */
@Data
@TableName("mg_datasource")
public class DataSource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 数据源编码（唯一）
     */
    private String datasourceCode;

    /**
     * 数据源类型：MYSQL, ORACLE, POSTGRESQL, SQLSERVER, DM, KINGBASE
     */
    private String datasourceType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String encryptedPassword;

    /**
     * 连接参数（JSON 格式）
     */
    private String connectionParams;

    /**
     * 数据源分组
     */
    private String groupName;

    /**
     * 环境标识：PROD, TEST, DEV
     */
    private String envType;

    /**
     * 描述
     */
    private String description;

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
