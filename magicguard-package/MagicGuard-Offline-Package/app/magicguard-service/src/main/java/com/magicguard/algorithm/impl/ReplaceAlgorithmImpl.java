package com.magicguard.algorithm.impl;

import com.magicguard.algorithm.MaskAlgorithm;

import java.util.Map;

/**
 * 替换脱敏算法
 * 例如：张三 -> 张*
 */
public class ReplaceAlgorithmImpl implements MaskAlgorithm {

    @Override
    public String mask(String data, Map<String, Object> params) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        String replaceChar = getStringParam(params, "replaceChar", "*");
        int visibleLen = getIntParam(params, "visibleLen", 1);

        int dataLen = data.length();
        if (dataLen <= visibleLen) {
            return repeat(replaceChar, dataLen);
        }

        StringBuilder result = new StringBuilder();
        result.append(data.substring(0, visibleLen));
        result.append(repeat(replaceChar, dataLen - visibleLen));

        return result.toString();
    }

    @Override
    public String getType() {
        return "REPLACE";
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
