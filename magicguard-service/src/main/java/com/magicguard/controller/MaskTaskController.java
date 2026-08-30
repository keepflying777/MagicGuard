package com.magicguard.controller;

import com.magicguard.entity.MaskTask;
import com.magicguard.service.MaskTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 脱敏任务 REST API
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class MaskTaskController {

    private final MaskTaskService taskService;

    public MaskTaskController(MaskTaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 创建脱敏任务
     */
    @PostMapping
    public ResponseEntity<MaskTask> createTask(@RequestBody Map<String, Object> request) {
        String taskName = (String) request.get("taskName");
        String sourceDatasourceCode = (String) request.get("sourceDatasourceCode");
        String targetType = (String) request.get("targetType");
        String sourceTables = (String) request.get("sourceTables");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> maskRules = (List<Map<String, Object>>) request.get("maskRules");
        String execType = (String) request.get("execType");
        String scheduleType = (String) request.get("scheduleType");

        MaskTask task = taskService.createTask(taskName, sourceDatasourceCode, targetType,
                sourceTables, maskRules, execType, scheduleType);
        return ResponseEntity.ok(task);
    }

    /**
     * 获取所有任务
     */
    @GetMapping
    public ResponseEntity<List<MaskTask>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaskTask> getTaskById(@PathVariable Long id) {
        MaskTask task = taskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    /**
     * 执行任务
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<MaskTask> executeTask(@PathVariable Long id) {
        try {
            MaskTask task = taskService.executeTask(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id) {
        try {
            taskService.cancelTask(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
}
