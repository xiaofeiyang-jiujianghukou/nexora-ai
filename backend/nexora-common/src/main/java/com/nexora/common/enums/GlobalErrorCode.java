package com.nexora.common.enums;

import com.nexora.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 */
@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    // ---- 系统级 ----
    SUCCESS(0, "成功"),
    INTERNAL_ERROR(10000, "系统内部错误"),
    PARAM_INVALID(10001, "参数校验失败"),
    SERVICE_UNAVAILABLE(10002, "服务不可用"),

    // ---- 认证/用户 (20000) ----
    UNAUTHORIZED(20001, "未登录"),
    TOKEN_EXPIRED(20002, "Token 已过期"),
    INVALID_CREDENTIALS(20003, "用户名或密码错误"),
    DUPLICATE_EMAIL(20004, "邮箱已注册"),
    USER_NOT_FOUND(20005, "用户不存在"),
    PERMISSION_DENIED(20006, "权限不足"),

    // ---- 新闻 (30000) ----
    NEWS_NOT_FOUND(30001, "新闻不存在"),
    NEWS_ARCHIVED(30002, "新闻已下架"),
    DUPLICATE_FAVORITE(30003, "已收藏，请勿重复操作"),
    CATEGORY_NOT_FOUND(30004, "分类不存在"),

    // ---- AI (40000) ----
    AI_ANALYSIS_FAILED(40001, "AI 分析失败"),
    AI_PROVIDER_UNAVAILABLE(40002, "AI 服务不可用"),
    AI_TASK_DUPLICATE(40003, "AI 任务重复，已跳过"),

    // ---- 搜索 (50000) ----
    SEARCH_ERROR(50001, "搜索服务异常"),
    INDEX_REBUILDING(50002, "索引重建中"),

    // ---- 外部服务 (60000) ----
    CRAWLER_SOURCE_UNREACHABLE(60001, "采集源不可达"),
    CRAWLER_PARSE_FAILED(60002, "采集解析失败");

    private final Integer code;
    private final String message;
}
