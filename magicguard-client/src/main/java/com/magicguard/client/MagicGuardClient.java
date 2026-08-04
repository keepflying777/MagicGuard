package com.magicguard.client;

import com.magicguard.client.core.CryptoClient;
import com.magicguard.client.util.SM4ClientUtil;

/**
 * MagicGuard 客户端入口类
 *
 * 使用示例：
 *
 * // 1. 创建客户端实例
 * MagicGuardClient client = new MagicGuardClient();
 *
 * // 2. 加密数据
 * String encrypted = client.encrypt("敏感数据");
 *
 * // 3. 解密数据
 * String decrypted = client.decrypt(encrypted);
 *
 * // 4. 使用指定密钥加密
 * String key = SM4ClientUtil.generateKey();
 * String iv = SM4ClientUtil.generateIV();
 * String encrypted2 = client.encrypt("敏感数据", key, iv);
 */
public class MagicGuardClient {

    private final CryptoClient cryptoClient;

    public MagicGuardClient() {
        this.cryptoClient = new CryptoClient();
    }

    public MagicGuardClient(String key, String iv) {
        this.cryptoClient = new CryptoClient(key, iv);
    }

    /**
     * 加密数据
     */
    public String encrypt(String plainData) {
        return cryptoClient.encrypt(plainData);
    }

    /**
     * 解密数据
     */
    public String decrypt(String encryptedData) {
        return cryptoClient.decrypt(encryptedData);
    }

    /**
     * 使用指定密钥加密
     */
    public String encrypt(String plainData, String key, String iv) {
        return cryptoClient.encrypt(plainData, key, iv);
    }

    /**
     * 使用指定密钥解密
     */
    public String decrypt(String encryptedData, String key, String iv) {
        return cryptoClient.decrypt(encryptedData, key, iv);
    }

    /**
     * 使用 ECB 模式加密
     */
    public String encryptECB(String plainData, String key) {
        return cryptoClient.encryptECB(plainData, key);
    }

    /**
     * 使用 ECB 模式解密
     */
    public String decryptECB(String encryptedData, String key) {
        return cryptoClient.decryptECB(encryptedData, key);
    }

    /**
     * 获取客户端版本
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * 生成新密钥
     */
    public static String generateKey() {
        return SM4ClientUtil.generateKey();
    }

    /**
     * 生成新 IV
     */
    public static String generateIV() {
        return SM4ClientUtil.generateIV();
    }

    public static void main(String[] args) {
        // 演示用法
        MagicGuardClient client = new MagicGuardClient();

        String plainText = "13812345678";
        String encrypted = client.encrypt(plainText);
        String decrypted = client.decrypt(encrypted);

        System.out.println("原文: " + plainText);
        System.out.println("加密: " + encrypted);
        System.out.println("解密: " + decrypted);
    }
}
