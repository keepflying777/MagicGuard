package com.magicguard.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM4 国密对称加密工具类
 * 支持 ECB 和 CBC 模式
 */
public class SM4Util {

    private static final String ALGORITHM = "SM4";
    private static final String ECB_TRANSFORMATION = "SM4/ECB/PKCS5Padding";
    private static final String CBC_TRANSFORMATION = "SM4/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 16; // 128 bits = 16 bytes
    private static final int IV_LENGTH = 16;

    static {
        // 注册 Bouncy Castle provider
        if (java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成随机 SM4 密钥
     *
     * @return 32 位十六进制字符串（128 位密钥）
     */
    public static String generateKey() {
        byte[] keyBytes = new byte[KEY_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);
        return Hex.toHexString(keyBytes);
    }

    /**
     * 生成随机 IV 向量（CBC 模式使用）
     */
    public static String generateIV() {
        byte[] ivBytes = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        return Hex.toHexString(ivBytes);
    }

    /**
     * ECB 模式加密
     *
     * @param data 明文数据
     * @param key  32 位十六进制字符串密钥
     * @return Base64 编码的密文
     */
    public static String encryptECB(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(Hex.decode(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ECB_TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB 加密失败", e);
        }
    }

    /**
     * ECB 模式解密
     *
     * @param encryptedData Base64 编码的密文
     * @param key           32 位十六进制字符串密钥
     * @return 明文
     */
    public static String decryptECB(String encryptedData, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(Hex.decode(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ECB_TRANSFORMATION, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB 解密失败", e);
        }
    }

    /**
     * CBC 模式加密
     *
     * @param data 明文数据
     * @param key  32 位十六进制字符串密钥
     * @param iv   32 位十六进制字符串 IV
     * @return Base64 编码的密文
     */
    public static String encryptCBC(String data, String key, String iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(Hex.decode(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(Hex.decode(iv));
            Cipher cipher = Cipher.getInstance(CBC_TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC 加密失败", e);
        }
    }

    /**
     * CBC 模式解密
     *
     * @param encryptedData Base64 编码的密文
     * @param key           32 位十六进制字符串密钥
     * @param iv            32 位十六进制字符串 IV
     * @return 明文
     */
    public static String decryptCBC(String encryptedData, String key, String iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(Hex.decode(key), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(Hex.decode(iv));
            Cipher cipher = Cipher.getInstance(CBC_TRANSFORMATION, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC 解密失败", e);
        }
    }

    /**
     * 验证密钥格式是否正确
     */
    public static boolean isValidKey(String key) {
        if (key == null || key.length() != 32) {
            return false;
        }
        try {
            Hex.decode(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
