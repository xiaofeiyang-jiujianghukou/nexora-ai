package com.nexora.search.repository;

import com.nexora.search.index.NewsDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * ES 新闻搜索仓库 — 仅在 ES 可用时生效
 */
@Repository
@ConditionalOnProperty(name = "nexora.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, Long> {
}
