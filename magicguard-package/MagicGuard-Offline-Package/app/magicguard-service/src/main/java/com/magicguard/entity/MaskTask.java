package com.magicguard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 静态脱敏任务实体
 */
@Data
@TableName("mg_mask_task")
public class MaskTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编码（唯一）
     */
    private String taskCode;

    /**
     * 源数据源 ID
     */
    private Long sourceDatasourceId;

    /**
     * 目标数据源 ID（可为 null，目标为文件时）
     */
    private Long targetDatasourceId;

    /**
     * 目标类型：DATABASE, FILE
     */
    private String targetType;

    /**
     * 目标文件路径（目标为文件时使用）
     */
    private String targetFilePath;

    /**
     * 源表名（多个用逗号分隔）
     */
    private String sourceTables;

    /**
     * 脱敏规则 JSON
     * 格式：[{"tableName": "user", "columns": [{"name": "id_card", "ruleId": 1}, {"name": "mobile", "ruleId": 2}]}]
     */
    private String maskRulesJson;

    /**
     * 执行类型：FULL, INCREMENT
     */
    private String execType;

    /**
     * 增量字段（按时间增量时使用）
     */
    private String incrementColumn;

    /**
     * 调度策略：IMMEDIATE, SCHEDULED, PERIODIC
     */
    private String scheduleType;

    /**
     * Cron 表达式（定时执行时使用）
     */
    private String cronExpression;

    /**
     * 任务状态：PENDING, RUNNING, SUCCESS, FAILED, CANCELLED
     */
    private String status;

    /**
     * 执行时间
     */
    private LocalDateTime execTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 是否需要审批：0-否, 1-是
     */
    private Integer needApproval;

    /**
     * 审批状态：APPROVED, REJECTED
     */
    private String approvalStatus;

    /**
     * 审批人
     */
    private String approver;

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
     * 创建人
     */
    private String createBy;
}
