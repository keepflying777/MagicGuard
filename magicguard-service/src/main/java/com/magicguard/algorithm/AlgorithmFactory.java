package com.magicguard.algorithm;

import com.magicguard.algorithm.impl.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 脱敏算法工厂
 */
@Component
public class AlgorithmFactory {

    private final Map<String, MaskAlgorithm> algorithms = new HashMap<>();

    public AlgorithmFactory() {
        // 注册内置算法
        algorithms.put("MASK", new MaskAlgorithmImpl());
        algorithms.put("REPLACE", new ReplaceAlgorithmImpl());
        algorithms.put("TRUNCATE", new TruncateAlgorithmImpl());
        algorithms.put("HASH", new HashAlgorithmImpl());
        algorithms.put("FPE", new FPEAlgorithmImpl());
    }

    /**
     * 获取算法实例
     */
    public MaskAlgorithm getAlgorithm(String type) {
        MaskAlgorithm algorithm = algorithms.get(type.toUpperCase());
        if (algorithm == null) {
            throw new IllegalArgumentException("不支持的脱敏算法类型: " + type);
        }
        return algorithm;
    }

    /**
     * 执行脱敏
     *
     * @param type   算法类型
     * @param data   原始数据
     * @param params 算法参数
     * @return 脱敏后的数据
     */
    public String execute(String type, String data, Map<String, Object> params) {
        MaskAlgorithm algorithm = getAlgorithm(type);
        return algorithm.mask(data, params);
    }

    /**
     * 获取所有支持的算法类型
     */
    public String[] getSupportedAlgorithms() {
        return algorithms.keySet().toArray(new String[0]);
    }
}
