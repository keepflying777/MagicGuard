package com.magicguard.controller;

import com.magicguard.entity.EncryptionKey;
import com.magicguard.service.KeyManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 密钥管理 REST API
 */
@RestController
@RequestMapping("/api/keys")
@CrossOrigin(origins = "*")
public class KeyManagementController {

    private final KeyManagementService keyService;

    public KeyManagementController(KeyManagementService keyService) {
        this.keyService = keyService;
    }

    /**
     * 生成新密钥
     */
    @PostMapping("/generate")
    public ResponseEntity<EncryptionKey> generateKey(
            @RequestParam String keyName,
            @RequestParam(required = false, defaultValue = "SM4") String algorithm,
            @RequestParam(required = false, defaultValue = "128") Integer keyLength,
            @RequestParam(required = false, defaultValue = "FIELD_ENCRYPT") String purpose) {

        EncryptionKey key = keyService.generateKey(keyName, algorithm, keyLength, purpose);
        return ResponseEntity.ok(key);
    }

    /**
     * 获取所有密钥
     */
    @GetMapping
    public ResponseEntity<List<EncryptionKey>> getAllKeys() {
        return ResponseEntity.ok(keyService.getAllKeys());
    }

    /**
     * 获取活跃密钥
     */
    @GetMapping("/active")
    public ResponseEntity<List<EncryptionKey>> getActiveKeys() {
        return ResponseEntity.ok(keyService.getActiveKeys());
    }

    /**
     * 根据 ID 获取密钥
     */
    @GetMapping("/{id}")
    public ResponseEntity<EncryptionKey> getKeyById(@PathVariable Long id) {
        EncryptionKey key = keyService.getKeyById(id);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(key);
    }

    /**
     * 密钥轮换
     */
    @PostMapping("/{id}/rotate")
    public ResponseEntity<EncryptionKey> rotateKey(@PathVariable Long id) {
        try {
            EncryptionKey newKey = keyService.rotateKey(id);
            return ResponseEntity.ok(newKey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 密钥销毁
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroyKey(@PathVariable Long id) {
        try {
            keyService.destroyKey(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 加密数据
     */
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encryptData(
            @RequestParam String data,
            @RequestParam Long keyId) {
        try {
            String encrypted = keyService.encryptData(data, keyId);
            return ResponseEntity.ok(Map.of("encryptedData", encrypted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 解密数据
     */
    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decryptData(
            @RequestParam String encryptedData,
            @RequestParam Long keyId) {
        try {
            String decrypted = keyService.decryptData(encryptedData, keyId);
            return ResponseEntity.ok(Map.of("data", decrypted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
