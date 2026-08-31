package com.magicguard.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magicguard.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataSourceRepository extends BaseMapper<DataSource> {
}
