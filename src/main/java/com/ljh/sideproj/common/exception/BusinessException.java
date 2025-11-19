package com.ljh.sideproj.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final String message;

    // 예: throw new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}