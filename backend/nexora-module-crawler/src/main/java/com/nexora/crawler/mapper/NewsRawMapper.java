package com.nexora.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexora.crawler.entity.NewsRawDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsRawMapper extends BaseMapper<NewsRawDO> {
}
