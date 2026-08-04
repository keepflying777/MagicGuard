package com.magicguard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magicguard.entity.DataSource;
import com.magicguard.repository.DataSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源管理服务
 */
@Service
public class DataSourceService {

    private final DataSourceRepository datasourceRepository;

    public DataSourceService(DataSourceRepository datasourceRepository) {
        this.datasourceRepository = datasourceRepository;
    }

    /**
     * 创建数据源
     */
    @Transactional
    public DataSource createDatasource(String name, String code, String type,
                                     String host, Integer port, String database,
                                     String username, String password, String groupName) {
        DataSource ds = new DataSource();
        ds.setDatasourceName(name);
        ds.setDatasourceCode(code);
        ds.setDatasourceType(type);
        ds.setHost(host);
        ds.setPort(port);
        ds.setDatabaseName(database);
        ds.setUsername(username);
        ds.setEncryptedPassword(password); // TODO: 实际应加密存储
        ds.setGroupName(groupName);
        ds.setEnabled(1);
        ds.setCreateTime(LocalDateTime.now());
        ds.setUpdateTime(LocalDateTime.now());

        datasourceRepository.insert(ds);
        return ds;
    }

    /**
     * 测试数据源连接
     */
    public boolean testConnection(Long id) {
        DataSource ds = datasourceRepository.selectById(id);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        try {
            String url = buildJdbcUrl(ds);
            try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getUsername())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有数据源
     */
    public List<DataSource> getAllDatasources() {
        return datasourceRepository.selectList(null);
    }

    /**
     * 根据 ID 获取
     */
    public DataSource getDatasourceById(Long id) {
        return datasourceRepository.selectById(id);
    }

    /**
     * 根据代码获取
     */
    public DataSource getDatasourceByCode(String code) {
        return datasourceRepository.selectOne(new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getDatasourceCode, code));
    }

    /**
     * 更新数据源
     */
    @Transactional
    public void updateDatasource(Long id, String name, String host, Integer port,
                                String database, String username, String password) {
        DataSource ds = datasourceRepository.selectById(id);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        if (name != null) ds.setDatasourceName(name);
        if (host != null) ds.setHost(host);
        if (port != null) ds.setPort(port);
        if (database != null) ds.setDatabaseName(database);
        if (username != null) ds.setUsername(username);
        if (password != null) ds.setEncryptedPassword(password);
        ds.setUpdateTime(LocalDateTime.now());

        datasourceRepository.updateById(ds);
    }

    /**
     * 删除数据源
     */
    @Transactional
    public void deleteDatasource(Long id) {
        datasourceRepository.deleteById(id);
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
            case "DM" -> String.format("jdbc:dm://%s:%d",
                    ds.getHost(), ds.getPort());
            default -> throw new RuntimeException("不支持的数据源类型: " + ds.getDatasourceType());
        };
    }
}
