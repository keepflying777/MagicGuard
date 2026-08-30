package com.magicguard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magicguard.algorithm.AlgorithmFactory;
import com.magicguard.entity.*;
import com.magicguard.repository.MaskTaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 静态脱敏任务服务
 */
@Service
public class MaskTaskService {

    private static final Logger log = LoggerFactory.getLogger(MaskTaskService.class);

    private final MaskTaskRepository taskRepository;
    private final MaskRuleService maskRuleService;
    private final KeyManagementService keyManagementService;
    private final DataSourceService dataSourceService;
    private final AlgorithmFactory algorithmFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaskTaskService(MaskTaskRepository taskRepository,
                          MaskRuleService maskRuleService,
                          KeyManagementService keyManagementService,
                          DataSourceService dataSourceService,
                          AlgorithmFactory algorithmFactory) {
        this.taskRepository = taskRepository;
        this.maskRuleService = maskRuleService;
        this.keyManagementService = keyManagementService;
        this.dataSourceService = dataSourceService;
        this.algorithmFactory = algorithmFactory;
    }

    /**
     * 创建脱敏任务
     */
    @Transactional
    public MaskTask createTask(String taskName, String sourceDatasourceCode,
                               String targetType, String sourceTables,
                               List<Map<String, Object>> maskRules,
                               String execType, String scheduleType) {
        DataSource sourceDs = dataSourceService.getDatasourceByCode(sourceDatasourceCode);
        if (sourceDs == null) {
            throw new RuntimeException("源数据源不存在: " + sourceDatasourceCode);
        }

        MaskTask task = new MaskTask();
        task.setTaskName(taskName);
        task.setTaskCode("TASK_" + System.currentTimeMillis());
        task.setSourceDatasourceId(sourceDs.getId());
        task.setTargetType(targetType != null ? targetType : "DATABASE");
        task.setSourceTables(sourceTables);
        task.setExecType(execType != null ? execType : "FULL");
        task.setScheduleType(scheduleType != null ? scheduleType : "IMMEDIATE");
        task.setStatus("PENDING");
        task.setNeedApproval(1);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        try {
            task.setMaskRulesJson(objectMapper.writeValueAsString(maskRules));
        } catch (Exception e) {
            throw new RuntimeException("脱敏规则序列化失败", e);
        }

        taskRepository.insert(task);
        return task;
    }

    /**
     * 执行脱敏任务
     */
    @Transactional
    public MaskTask executeTask(Long taskId) {
        MaskTask task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        task.setStatus("RUNNING");
        task.setExecTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskRepository.updateById(task);

        try {
            // 解析脱敏规则
            List<Map<String, Object>> maskRules = objectMapper.readValue(
                    task.getMaskRulesJson(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 获取源数据源
            DataSource sourceDs = dataSourceService.getDatasourceById(task.getSourceDatasourceId());

            // 获取目标数据源
            DataSource targetDs = null;
            if (task.getTargetDatasourceId() != null) {
                targetDs = dataSourceService.getDatasourceById(task.getTargetDatasourceId());
            }

            // 执行脱敏
            executeMask(task.getSourceTables(), maskRules, sourceDs, targetDs, task.getTargetType());

            task.setStatus("SUCCESS");
            task.setFinishTime(LocalDateTime.now());
            task.setMessage("脱敏任务执行成功");
        } catch (Exception e) {
            log.error("脱敏任务执行失败", e);
            task.setStatus("FAILED");
            task.setFinishTime(LocalDateTime.now());
            task.setMessage("执行失败: " + e.getMessage());
        }

        taskRepository.updateById(task);
        return task;
    }

    /**
     * 执行脱敏逻辑
     */
    private void executeMask(String sourceTables, List<Map<String, Object>> maskRules,
                            DataSource sourceDs, DataSource targetDs, String targetType) throws Exception {
        // 解析表名
        String[] tables = sourceTables.split(",");

        for (String tableName : tables) {
            tableName = tableName.trim();
            log.info("开始处理表: {}", tableName);

            // 查找该表的脱敏规则
            Map<String, Long> columnRuleMap = new HashMap<>();
            for (Map<String, Object> rule : maskRules) {
                if (tableName.equals(rule.get("tableName"))) {
                    List<Map<String, Object>> columns = (List<Map<String, Object>>) rule.get("columns");
                    if (columns != null) {
                        for (Map<String, Object> col : columns) {
                            String colName = (String) col.get("name");
                            Long ruleId = ((Number) col.get("ruleId")).longValue();
                            columnRuleMap.put(colName, ruleId);
                        }
                    }
                }
            }

            if (columnRuleMap.isEmpty()) {
                log.info("表 {} 没有配置脱敏规则，跳过", tableName);
                continue;
            }

            // 从源数据库读取数据
            List<Map<String, Object>> data = readFromSource(sourceDs, tableName);

            // 执行脱敏
            List<Map<String, Object>> maskedData = applyMask(data, columnRuleMap);

            // 写入目标
            if ("DATABASE".equals(targetType) && targetDs != null) {
                writeToTarget(targetDs, tableName, maskedData);
            }

            log.info("表 {} 处理完成，共 {} 条数据", tableName, data.size());
        }
    }

    /**
     * 从源数据库读取数据
     */
    private List<Map<String, Object>> readFromSource(DataSource ds, String tableName) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        String url = buildJdbcUrl(ds);
        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getEncryptedPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                result.add(row);
            }
        }

        return result;
    }

    /**
     * 应用脱敏规则
     */
    private List<Map<String, Object>> applyMask(List<Map<String, Object>> data,
                                                Map<String, Long> columnRuleMap) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> row : data) {
            Map<String, Object> maskedRow = new LinkedHashMap<>(row);

            for (Map.Entry<String, Long> entry : columnRuleMap.entrySet()) {
                String columnName = entry.getKey();
                Long ruleId = entry.getValue();

                if (row.containsKey(columnName) && row.get(columnName) != null) {
                    String originalValue = row.get(columnName).toString();
                    String maskedValue = applyRule(originalValue, ruleId);
                    maskedRow.put(columnName, maskedValue);
                }
            }

            result.add(maskedRow);
        }

        return result;
    }

    /**
     * 应用单条脱敏规则
     */
    private String applyRule(String data, Long ruleId) {
        MaskRule rule = maskRuleService.getRuleById(ruleId);
        if (rule == null) {
            return data;
        }

        Map<String, Object> params = maskRuleService.parseAlgorithmParams(rule.getAlgorithmParams());
        return algorithmFactory.execute(rule.getAlgorithmType(), data, params);
    }

    /**
     * 写入目标数据库
     */
    private void writeToTarget(DataSource ds, String tableName, List<Map<String, Object>> data) throws Exception {
        if (data.isEmpty()) {
            return;
        }

        String url = buildJdbcUrl(ds);
        Map<String, Object> firstRow = data.get(0);
        String[] columns = firstRow.keySet().toArray(new String[0]);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        sql.append(String.join(",", columns));
        sql.append(") VALUES (");
        sql.append("?".repeat(columns.length));
        sql.append(")");

        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getEncryptedPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (Map<String, Object> row : data) {
                for (int i = 0; i < columns.length; i++) {
                    pstmt.setObject(i + 1, row.get(columns[i]));
                }
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }
    }

    /**
     * 构建 JDBC URL
     */
    private String buildJdbcUrl(DataSource ds) {
        return switch (ds.getDatasourceType()) {
            case "MYSQL" -> String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            case "ORACLE" -> String.format("jdbc:oracle:thin:@%s:%d:%s",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            case "POSTGRESQL" -> String.format("jdbc:postgresql://%s:%d/%s",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            case "SQLSERVER" -> String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            default -> throw new RuntimeException("不支持的数据源类型: " + ds.getDatasourceType());
        };
    }

    /**
     * 获取任务列表
     */
    public List<MaskTask> getAllTasks() {
        return taskRepository.selectList(new LambdaQueryWrapper<MaskTask>()
                .orderByDesc(MaskTask::getCreateTime));
    }

    /**
     * 获取任务详情
     */
    public MaskTask getTaskById(Long id) {
        return taskRepository.selectById(id);
    }

    /**
     * 取消任务
     */
    @Transactional
    public void cancelTask(Long id) {
        MaskTask task = taskRepository.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"PENDING".equals(task.getStatus()) && !"RUNNING".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许取消");
        }

        task.setStatus("CANCELLED");
        task.setUpdateTime(LocalDateTime.now());
        taskRepository.updateById(task);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * 更新任务
     */
    @Transactional
    public void updateTask(Long id, String taskName, String sourceTables,
                           List<Map<String, Object>> maskRules,
                           String execType, String scheduleType) {
        MaskTask task = taskRepository.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (taskName != null) task.setTaskName(taskName);
        if (sourceTables != null) task.setSourceTables(sourceTables);
        if (execType != null) task.setExecType(execType);
        if (scheduleType != null) task.setScheduleType(scheduleType);
        if (maskRules != null) {
            try {
                task.setMaskRulesJson(objectMapper.writeValueAsString(maskRules));
            } catch (Exception e) {
                throw new RuntimeException("脱敏规则序列化失败", e);
            }
        }
        task.setUpdateTime(LocalDateTime.now());
        taskRepository.updateById(task);
    }
}
