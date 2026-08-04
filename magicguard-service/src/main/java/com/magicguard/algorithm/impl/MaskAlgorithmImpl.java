package com.magicguard.algorithm.impl;

import com.magicguard.algorithm.MaskAlgorithm;

import java.util.Map;

/**
 * 掩码脱敏算法
 * 例如：13812345678 -> 138****5678
 */
public class MaskAlgorithmImpl implements MaskAlgorithm {

    @Override
    public String mask(String data, Map<String, Object> params) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        int prefixLen = getIntParam(params, "prefixLen", 3);
        int suffixLen = getIntParam(params, "suffixLen", 4);
        String maskChar = getStringParam(params, "maskChar", "*");

        int dataLen = data.length();
        if (dataLen <= prefixLen + suffixLen) {
            // 数据长度不够，全部掩码
            return repeat(maskChar, dataLen);
        }

        StringBuilder result = new StringBuilder();
        result.append(data.substring(0, prefixLen));
        result.append(repeat(maskChar, dataLen - prefixLen - suffixLen));
        result.append(data.substring(dataLen - suffixLen));

        return result.toString();
    }

    @Override
    public String getType() {
        return "MASK";
    }

    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        if (params != null && params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return defaultValue;
    }

    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params != null && params.containsKey(key)) {
            Object value = params.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }

    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
