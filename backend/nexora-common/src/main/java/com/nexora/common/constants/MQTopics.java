package com.nexora.common.constants;

/**
 * RocketMQ Topic 常量
 */
public interface MQTopics {

    /** 新闻采集完成 */
    String NEWS_COLLECTED = "nexora-news-collected";

    /** AI 分析任务 */
    String NEWS_AI_TASK = "nexora-news-ai-task";

    /** ES 索引任务 */
    String NEWS_INDEX_TASK = "nexora-news-index-task";

    /** 事件聚合任务 */
    String NEWS_EVENT_TASK = "nexora-news-event-task";

    /** 语言增量回填任务 */
    String LANG_BACKFILL = "nexora-news-lang-backfill";

    /** 用户通知 */
    String USER_NOTIFICATION = "nexora-user-notification";
}
