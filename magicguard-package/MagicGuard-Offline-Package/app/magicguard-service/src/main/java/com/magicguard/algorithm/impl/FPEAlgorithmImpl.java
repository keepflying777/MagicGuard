package com.magicguard.algorithm.impl;

import com.magicguard.algorithm.MaskAlgorithm;
import com.magicguard.util.SM4Util;

import java.util.Map;

/**
 * FPE（Format Preserving Encryption）保形加密算法简化实现
 * 使用 SM4 ECB 模式，保持格式一致
 * 注意：这是简化版，生产环境应使用标准的 FF1/FF3 算法
 */
public class FPEAlgorithmImpl implements MaskAlgorithm {

    /**
     * 默认密钥（实际应从密钥管理获取）
     */
    private static final String DEFAULT_KEY = "0123456789ABCDEF0123456789ABCDEF";

    @Override
    public String mask(String data, Map<String, Object> params) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        String key = getStringParam(params, "key", DEFAULT_KEY);

        try {
            // 使用 ECB 模式加密（简化版，不推荐用于生产）
            return SM4Util.encryptECB(data, key);
        } catch (Exception e) {
            throw new RuntimeException("FPE 加密失败", e);
        }
    }

    /**
     * FPE 解密（可逆还原）
     */
    public String unmask(String encryptedData, Map<String, Object> params) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        String key = getStringParam(params, "key", DEFAULT_KEY);

        try {
            return SM4Util.decryptECB(encryptedData, key);
        } catch (Exception e) {
            throw new RuntimeException("FPE 解密失败", e);
        }
    }

    @Override
    public String getType() {
        return "FPE";
    }

    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params != null && params.containsKey(key)) {
            Object value = params.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }
}
