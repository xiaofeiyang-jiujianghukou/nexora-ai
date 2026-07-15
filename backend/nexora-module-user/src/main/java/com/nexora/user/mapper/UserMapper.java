package com.nexora.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexora.user.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
