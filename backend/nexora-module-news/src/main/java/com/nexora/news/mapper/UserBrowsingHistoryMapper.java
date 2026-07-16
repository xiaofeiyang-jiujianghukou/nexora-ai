package com.nexora.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexora.news.entity.UserBrowsingHistoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBrowsingHistoryMapper extends BaseMapper<UserBrowsingHistoryDO> {
}
