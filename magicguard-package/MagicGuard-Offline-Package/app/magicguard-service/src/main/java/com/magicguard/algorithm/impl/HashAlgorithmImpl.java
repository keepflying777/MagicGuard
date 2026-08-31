package com.magicguard.algorithm.impl;

import com.magicguard.algorithm.MaskAlgorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Hash 脱敏算法
 * 使用 SHA-256 哈希，可选截断
 */
public class HashAlgorithmImpl implements MaskAlgorithm {

    @Override
    public String mask(String data, Map<String, Object> params) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        int truncateLen = getIntParam(params, "truncateLen", 16);
        String salt = getStringParam(params, "salt", "");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = data + salt;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().substring(0, Math.min(truncateLen, hexString.length()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    @Override
    public String getType() {
        return "HASH";
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
}
