package com.magicguard.algorithm;

import java.util.Map;

/**
 * 脱敏算法接口
 */
public interface MaskAlgorithm {

    /**
     * 执行脱敏
     *
     * @param data    原始数据
     * @param params  算法参数
     * @return 脱敏后的数据
     */
    String mask(String data, Map<String, Object> params);

    /**
     * 获取算法类型
     */
    String getType();
}
