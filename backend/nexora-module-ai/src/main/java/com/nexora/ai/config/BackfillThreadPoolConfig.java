package com.nexora.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 语言回填专用线程池 — 并行调用 LLM 加速增量语言生成
 */
@Slf4j
@Configuration
public class BackfillThreadPoolConfig {

    @Bean("backfillExecutor")
    public ThreadPoolExecutor backfillExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,                                      // corePoolSize: 5 线程并行
                10,                                     // maxPoolSize: 峰值 10
                60, TimeUnit.SECONDS,                  // keepAlive: 空闲线程 60s 回收
                new LinkedBlockingQueue<>(100),         // 有界队列
                r -> {
                    Thread t = new Thread(r, "backfill-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时回退到调用线程
        );
        log.info("Backfill thread pool initialized: core=5, max=10, queue=100");
        return executor;
    }
}
