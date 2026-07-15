package com.nexora.crawler.collector;

import java.util.List;
import java.util.Map;

/**
 * 新闻采集器接口 — 一接口多实现 (RSS/API/Crawler)
 */
public interface NewsCollector {

    /**
     * 采集新闻原始数据
     * @return 原始新闻列表，每条为 Map 格式 {title, content, url, author, publishTime}
     */
    List<Map<String, String>> collect();

    /**
     * 采集器名称
     */
    String getName();
}
