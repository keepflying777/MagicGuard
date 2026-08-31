package com.magicguard.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magicguard.entity.MaskRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaskRuleRepository extends BaseMapper<MaskRule> {
}
