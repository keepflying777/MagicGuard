package com.magicguard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 脱敏规则实体
 */
@Data
@TableName("mg_mask_rule")
public class MaskRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则编码（唯一）
     */
    private String ruleCode;

    /**
     * 脱敏算法类型：MASK, REPLACE, TRUNCATE, FPE, HASH, RANDOM
     */
    private String algorithmType;

    /**
     * 算法参数（JSON 格式）
     * 如：{"prefixLen": 3, "suffixLen": 4, "maskChar": "*"}
     */
    private String algorithmParams;

    /**
     * 目标字段类型：COLUMN, FILE
     */
    private String targetType;

    /**
     * 关联数据源 ID（可选）
     */
    private Long datasourceId;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

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
