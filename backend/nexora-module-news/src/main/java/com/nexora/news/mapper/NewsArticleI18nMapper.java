package com.nexora.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexora.news.entity.NewsArticleI18nDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻多语言内容 Mapper
 */
@Mapper
public interface NewsArticleI18nMapper extends BaseMapper<NewsArticleI18nDO> {
}
