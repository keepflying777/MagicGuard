package com.magicguard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magicguard.entity.EncryptionKey;
import com.magicguard.repository.EncryptionKeyRepository;
import com.magicguard.util.SM4Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 密钥管理服务
 */
@Service
public class KeyManagementService {

    private final EncryptionKeyRepository keyRepository;
    private final String masterKey;

    /**
     * 主密钥，用于加密 DEK（数据加密密钥）
     * 生产环境应从配置文件或环境变量读取
     */
    public KeyManagementService(
            EncryptionKeyRepository keyRepository,
            @Value("${magicguard.sm4-key}") String masterKey) {
        this.keyRepository = keyRepository;
        this.masterKey = masterKey;
    }

    /**
     * 生成新密钥
     */
    @Transactional
    public EncryptionKey generateKey(String keyName, String algorithm, Integer keyLength, String purpose) {
        // 生成随机密钥
        String keyData = SM4Util.generateKey();

        // 用主密钥加密存储
        String iv = SM4Util.generateIV();
        String encryptedKey = SM4Util.encryptCBC(keyData, masterKey, iv);

        EncryptionKey key = new EncryptionKey();
        key.setKeyName(keyName);
        key.setKeyCode("KEY_" + System.currentTimeMillis());
        key.setAlgorithm(algorithm != null ? algorithm : "SM4");
        key.setKeyLength(keyLength != null ? keyLength : 128);
        key.setEncryptedKey(encryptedKey);
        key.setIv(iv);
        key.setStatus("ACTIVE");
        key.setPurpose(purpose != null ? purpose : "FIELD_ENCRYPT");
        key.setCreateBy("system");
        key.setCreateTime(LocalDateTime.now());

        keyRepository.insert(key);
        return key;
    }

    /**
     * 根据 ID 获取密钥
     */
    public EncryptionKey getKeyById(Long id) {
        return keyRepository.selectById(id);
    }

    /**
     * 根据密钥代码获取密钥
     */
    public EncryptionKey getKeyByCode(String keyCode) {
        return keyRepository.selectOne(new LambdaQueryWrapper<EncryptionKey>()
                .eq(EncryptionKey::getKeyCode, keyCode));
    }

    /**
     * 获取所有密钥
     */
    public List<EncryptionKey> getAllKeys() {
        return keyRepository.selectList(null);
    }

    /**
     * 获取活跃密钥
     */
    public List<EncryptionKey> getActiveKeys() {
        return keyRepository.selectList(new LambdaQueryWrapper<EncryptionKey>()
                .eq(EncryptionKey::getStatus, "ACTIVE"));
    }

    /**
     * 密钥轮换 - 将旧密钥标记为 ROTATED，生成新密钥
     */
    @Transactional
    public EncryptionKey rotateKey(Long keyId) {
        EncryptionKey oldKey = keyRepository.selectById(keyId);
        if (oldKey == null) {
            throw new RuntimeException("密钥不存在");
        }

        // 标记旧密钥为轮换状态
        oldKey.setStatus("ROTATED");
        oldKey.setUpdateTime(LocalDateTime.now());
        keyRepository.updateById(oldKey);

        // 生成新密钥
        return generateKey(
                oldKey.getKeyName() + "_ROTATED",
                oldKey.getAlgorithm(),
                oldKey.getKeyLength(),
                oldKey.getPurpose()
        );
    }

    /**
     * 密钥销毁 - 逻辑销毁
     */
    @Transactional
    public void destroyKey(Long keyId) {
        EncryptionKey key = keyRepository.selectById(keyId);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        key.setStatus("DESTROYED");
        key.setUpdateTime(LocalDateTime.now());
        keyRepository.updateById(key);
    }

    /**
     * 解密数据（内部使用，密钥明文不返回）
     * 注意：此方法解密的是用该密钥加密的数据，返回解密后的明文
     */
    public String decryptData(String encryptedData, Long keyId) {
        EncryptionKey key = keyRepository.selectById(keyId);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        if (!"ACTIVE".equals(key.getStatus())) {
            throw new RuntimeException("密钥状态异常，无法解密");
        }

        // 解密 DEK 获取明文
        String keyData = SM4Util.decryptCBC(key.getEncryptedKey(), masterKey, key.getIv());

        // 用 DEK 解密数据
        return SM4Util.decryptCBC(encryptedData, keyData, key.getIv());
    }

    /**
     * 加密数据（内部使用）
     */
    public String encryptData(String plainData, Long keyId) {
        EncryptionKey key = keyRepository.selectById(keyId);
        if (key == null) {
            throw new RuntimeException("密钥不存在");
        }
        if (!"ACTIVE".equals(key.getStatus())) {
            throw new RuntimeException("密钥状态异常，无法加密");
        }

        // 解密 DEK 获取明文
        String keyData = SM4Util.decryptCBC(key.getEncryptedKey(), masterKey, key.getIv());

        // 用 DEK 加密数据
        return SM4Util.encryptCBC(plainData, keyData, key.getIv());
    }
}
