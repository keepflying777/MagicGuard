package com.magicguard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.magicguard.entity.MaskRule;
import com.magicguard.repository.MaskRuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 脱敏规则服务
 */
@Service
public class MaskRuleService {

    private final MaskRuleRepository ruleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaskRuleService(MaskRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * 创建脱敏规则
     */
    @Transactional
    public MaskRule createRule(String ruleName, String ruleCode, String algorithmType,
                               Map<String, Object> algorithmParams, Integer priority) {
        MaskRule rule = new MaskRule();
        rule.setRuleName(ruleName);
        rule.setRuleCode(ruleCode);
        rule.setAlgorithmType(algorithmType);
        rule.setEnabled(1);
        rule.setPriority(priority != null ? priority : 100);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());

        try {
            rule.setAlgorithmParams(objectMapper.writeValueAsString(algorithmParams));
        } catch (Exception e) {
            throw new RuntimeException("算法参数序列化失败", e);
        }

        ruleRepository.insert(rule);
        return rule;
    }

    /**
     * 获取所有规则
     */
    public List<MaskRule> getAllRules() {
        return ruleRepository.selectList(new LambdaQueryWrapper<MaskRule>()
                .orderByAsc(MaskRule::getPriority));
    }

    /**
     * 获取启用的规则
     */
    public List<MaskRule> getEnabledRules() {
        return ruleRepository.selectList(new LambdaQueryWrapper<MaskRule>()
                .eq(MaskRule::getEnabled, 1)
                .orderByAsc(MaskRule::getPriority));
    }

    /**
     * 根据 ID 获取规则
     */
    public MaskRule getRuleById(Long id) {
        return ruleRepository.selectById(id);
    }

    /**
     * 根据规则代码获取规则
     */
    public MaskRule getRuleByCode(String ruleCode) {
        return ruleRepository.selectOne(new LambdaQueryWrapper<MaskRule>()
                .eq(MaskRule::getRuleCode, ruleCode));
    }

    /**
     * 更新规则
     */
    @Transactional
    public void updateRule(Long id, String ruleName, String algorithmType,
                           Map<String, Object> algorithmParams, Integer priority) {
        MaskRule rule = ruleRepository.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }

        if (ruleName != null) {
            rule.setRuleName(ruleName);
        }
        if (algorithmType != null) {
            rule.setAlgorithmType(algorithmType);
        }
        if (priority != null) {
            rule.setPriority(priority);
        }
        if (algorithmParams != null) {
            try {
                rule.setAlgorithmParams(objectMapper.writeValueAsString(algorithmParams));
            } catch (Exception e) {
                throw new RuntimeException("算法参数序列化失败", e);
            }
        }
        rule.setUpdateTime(LocalDateTime.now());

        ruleRepository.updateById(rule);
    }

    /**
     * 启用/禁用规则
     */
    @Transactional
    public void setRuleEnabled(Long id, boolean enabled) {
        ruleRepository.update(null, new LambdaUpdateWrapper<MaskRule>()
                .eq(MaskRule::getId, id)
                .set(MaskRule::getEnabled, enabled ? 1 : 0)
                .set(MaskRule::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 删除规则
     */
    @Transactional
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    /**
     * 解析算法参数
     */
    public Map<String, Object> parseAlgorithmParams(String paramsJson) {
        if (paramsJson == null || paramsJson.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(paramsJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("算法参数解析失败", e);
        }
    }
}
