package com.magicguard.client.core;

import com.magicguard.client.util.SM4ClientUtil;

/**
 * 客户端加解密核心接口
 * 提供本地加解密能力，支持从服务端获取密钥或使用本地密钥
 */
public class CryptoClient {

    private String defaultKey;
    private String defaultIV;

    public CryptoClient() {
        // 生成默认密钥（生产环境应从服务端获取或配置）
        this.defaultKey = SM4ClientUtil.generateKey();
        this.defaultIV = SM4ClientUtil.generateIV();
    }

    public CryptoClient(String key, String iv) {
        this.defaultKey = key;
        this.defaultIV = iv;
    }

    /**
     * 加密数据（使用默认密钥，CBC 模式）
     */
    public String encrypt(String plainData) {
        return SM4ClientUtil.encryptCBC(plainData, defaultKey, defaultIV);
    }

    /**
     * 解密数据（使用默认密钥，CBC 模式）
     */
    public String decrypt(String encryptedData) {
        return SM4ClientUtil.decryptCBC(encryptedData, defaultKey, defaultIV);
    }

    /**
     * 加密数据（指定密钥）
     */
    public String encrypt(String plainData, String key, String iv) {
        return SM4ClientUtil.encryptCBC(plainData, key, iv);
    }

    /**
     * 解密数据（指定密钥）
     */
    public String decrypt(String encryptedData, String key, String iv) {
        return SM4ClientUtil.decryptCBC(encryptedData, key, iv);
    }

    /**
     * 加密数据（ECB 模式）
     */
    public String encryptECB(String plainData, String key) {
        return SM4ClientUtil.encryptECB(plainData, key);
    }

    /**
     * 解密数据（ECB 模式）
     */
    public String decryptECB(String encryptedData, String key) {
        return SM4ClientUtil.decryptECB(encryptedData, key);
    }

    /**
     * 获取默认密钥
     */
    public String getDefaultKey() {
        return defaultKey;
    }

    /**
     * 获取默认 IV
     */
    public String getDefaultIV() {
        return defaultIV;
    }

    /**
     * 生成新的密钥
     */
    public static String generateKey() {
        return SM4ClientUtil.generateKey();
    }

    /**
     * 生成新的 IV
     */
    public static String generateIV() {
        return SM4ClientUtil.generateIV();
    }
}
