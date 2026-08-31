package com.magicguard.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magicguard.entity.EncryptionKey;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EncryptionKeyRepository extends BaseMapper<EncryptionKey> {
}
