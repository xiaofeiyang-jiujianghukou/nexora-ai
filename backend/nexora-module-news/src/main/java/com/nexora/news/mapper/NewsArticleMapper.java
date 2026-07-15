package com.nexora.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexora.news.entity.NewsArticleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻文章 Mapper
 */
@Mapper
public interface NewsArticleMapper extends BaseMapper<NewsArticleDO> {
}
