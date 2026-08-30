package com.magicguard.controller;

import com.magicguard.entity.DataSource;
import com.magicguard.service.DataSourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理 REST API
 */
@RestController
@RequestMapping("/api/datasources")
@CrossOrigin(origins = "*")
public class DataSourceController {

    private final DataSourceService datasourceService;

    public DataSourceController(DataSourceService datasourceService) {
        this.datasourceService = datasourceService;
    }

    /**
     * 创建数据源
     */
    @PostMapping
    public ResponseEntity<DataSource> createDatasource(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String code = (String) request.get("code");
        String type = (String) request.get("type");
        String host = (String) request.get("host");
        Integer port = ((Number) request.get("port")).intValue();
        String database = (String) request.get("database");
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String groupName = (String) request.get("groupName");

        DataSource ds = datasourceService.createDatasource(name, code, type, host, port,
                database, username, password, groupName);
        return ResponseEntity.ok(ds);
    }

    /**
     * 获取所有数据源
     */
    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDatasources() {
        return ResponseEntity.ok(datasourceService.getAllDatasources());
    }

    /**
     * 根据 ID 获取数据源
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDatasourceById(@PathVariable Long id) {
        DataSource ds = datasourceService.getDatasourceById(id);
        if (ds == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ds);
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long id) {
        try {
            boolean success = datasourceService.testConnection(id);
            return ResponseEntity.ok(Map.of("success", success, "message",
                    success ? "连接成功" : "连接失败"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 获取数据源的所有表
     */
    @GetMapping("/{id}/tables")
    public ResponseEntity<List<String>> getTables(@PathVariable Long id) {
        return ResponseEntity.ok(datasourceService.getTables(id));
    }

    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDatasource(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String host = (String) request.get("host");
        Integer port = request.get("port") != null ? ((Number) request.get("port")).intValue() : null;
        String database = (String) request.get("database");
        String username = (String) request.get("username");
        String password = (String) request.get("password");

        datasourceService.updateDatasource(id, name, host, port, database, username, password);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatasource(@PathVariable Long id) {
        datasourceService.deleteDatasource(id);
        return ResponseEntity.ok().build();
    }
}
