package com.magicguard.algorithm.impl;

import com.magicguard.algorithm.MaskAlgorithm;

import java.util.Map;

/**
 * 截断脱敏算法
 * 例如：example@email.com -> a***@email.com
 */
public class TruncateAlgorithmImpl implements MaskAlgorithm {

    @Override
    public String mask(String data, Map<String, Object> params) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        int prefixLen = getIntParam(params, "prefixLen", 1);
        String domain = getStringParam(params, "domain", "");

        if (data.contains("@") && domain.isEmpty()) {
            // 邮箱截断
            int atIndex = data.indexOf('@');
            String localPart = data.substring(0, atIndex);
            String domainPart = data.substring(atIndex);

            if (localPart.length() <= prefixLen) {
                return repeat("*", localPart.length()) + domainPart;
            }

            return localPart.substring(0, prefixLen) + "***" + domainPart;
        }

        // 普通截断
        if (data.length() <= prefixLen) {
            return repeat("*", data.length());
        }

        return data.substring(0, prefixLen) + "***";
    }

    @Override
    public String getType() {
        return "TRUNCATE";
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
