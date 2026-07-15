package com.nexora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nexora AI — 应用主入口
 * <p>
 * 唯一的 @SpringBootApplication，汇集所有业务模块。
 * 其他模块禁止声明 @SpringBootApplication。
 */
@SpringBootApplication
public class NexoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraApplication.class, args);
    }
}
