package com.nexora.common.exception;

/**
 * 错误码接口 — 所有错误枚举必须实现
 */
public interface ErrorCode {

    Integer getCode();

    String getMessage();
}
