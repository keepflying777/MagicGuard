package com.magicguard.client.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM4 客户端加解密工具类
 * 与服务端 SM4Util 功能一致，供客户端独立加解密使用
 */
public class SM4ClientUtil {

    private static final String ALGORITHM = "SM4";
    private static final String ECB_TRANSFORMATION = "SM4/ECB/PKCS5Padding";
    private static final String CBC_TRANSFORMATION = "SM4/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    static {
        if (java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成随机 SM4 密钥
     */
    public static String generateKey() {
        byte[] keyBytes = new byte[KEY_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);
        return Hex.toHexString(keyBytes);
    }

    /**
     * 生成随机 IV
     */
    public static String generateIV() {
        byte[] ivBytes = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        return Hex.toHexString(ivBytes);
    }

    /**
     * ECB 模式加密
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
}
