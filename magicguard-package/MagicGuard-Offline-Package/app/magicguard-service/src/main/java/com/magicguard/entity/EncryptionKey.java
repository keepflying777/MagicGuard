package com.magicguard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 加密密钥实体
 */
@Data
@TableName("mg_encryption_key")
public class EncryptionKey {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 密钥名称
     */
    private String keyName;

    /**
     * 密钥标识（唯一）
     */
    private String keyCode;

    /**
     * 算法类型：SM4, AES
     */
    private String algorithm;

    /**
     * 密钥长度：128, 256
     */
    private Integer keyLength;

    /**
     * 加密后的密钥密文（SM4 加密主密钥后的结果）
     */
    private String encryptedKey;

    /**
     * IV 向量（CBC 模式使用）
     */
    private String iv;

    /**
     * 密钥状态：ACTIVE, ROTATED, REVOKED, DESTROYED
     */
    private String status;

    /**
     * 用途标识：FIELD_ENCRYPT, DATA_MASK, KEY_WRAP
     */
    private String purpose;

    /**
     * 失效时间（可选，用于定时轮换）
     */
    private LocalDateTime expireTime;

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

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;
}
