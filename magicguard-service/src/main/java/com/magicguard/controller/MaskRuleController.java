package com.magicguard.controller;

import com.magicguard.algorithm.AlgorithmFactory;
import com.magicguard.entity.MaskRule;
import com.magicguard.service.MaskRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 脱敏规则管理 REST API
 */
@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "*")
public class MaskRuleController {

    private final MaskRuleService ruleService;
    private final AlgorithmFactory algorithmFactory;

    public MaskRuleController(MaskRuleService ruleService, AlgorithmFactory algorithmFactory) {
        this.ruleService = ruleService;
        this.algorithmFactory = algorithmFactory;
    }

    /**
     * 创建脱敏规则
     */
    @PostMapping
    public ResponseEntity<MaskRule> createRule(@RequestBody Map<String, Object> request) {
        String ruleName = (String) request.get("ruleName");
        String ruleCode = (String) request.get("ruleCode");
        String algorithmType = (String) request.get("algorithmType");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Integer priority = request.get("priority") != null ?
                ((Number) request.get("priority")).intValue() : null;

        MaskRule rule = ruleService.createRule(ruleName, ruleCode, algorithmType, params, priority);
        return ResponseEntity.ok(rule);
    }

    /**
     * 获取所有规则
     */
    @GetMapping
    public ResponseEntity<List<MaskRule>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    /**
     * 获取启用的规则
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<MaskRule>> getEnabledRules() {
        return ResponseEntity.ok(ruleService.getEnabledRules());
    }

    /**
     * 根据 ID 获取规则
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaskRule> getRuleById(@PathVariable Long id) {
        MaskRule rule = ruleService.getRuleById(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String ruleName = (String) request.get("ruleName");
        String algorithmType = (String) request.get("algorithmType");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        Integer priority = request.get("priority") != null ?
                ((Number) request.get("priority")).intValue() : null;

        ruleService.updateRule(id, ruleName, algorithmType, params, priority);
        return ResponseEntity.ok().build();
    }

    /**
     * 启用/禁用规则
     */
    @PutMapping("/{id}/enabled")
    public ResponseEntity<Void> setRuleEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        ruleService.setRuleEnabled(id, enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取支持的算法类型
     */
    @GetMapping("/algorithms")
    public ResponseEntity<String[]> getSupportedAlgorithms() {
        return ResponseEntity.ok(algorithmFactory.getSupportedAlgorithms());
    }

    /**
     * 测试脱敏效果
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testMask(@RequestBody Map<String, Object> request) {
        String algorithmType = (String) request.get("algorithmType");
        String data = (String) request.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");

        try {
            String masked = algorithmFactory.execute(algorithmType, data, params);
            return ResponseEntity.ok(Map.of("original", data, "masked", masked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
