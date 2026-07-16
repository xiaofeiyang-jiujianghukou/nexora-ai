package com.nexora.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语言增量回填事件 — 通过 MQ 异步触发批量回填
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LangBackfillEvent {

    /** 目标语言代码: zh, en, ja, ko, fr, de, ru */
    private String langCode;

    /** 本次回填文章数上限 */
    private int batchSize;
}
