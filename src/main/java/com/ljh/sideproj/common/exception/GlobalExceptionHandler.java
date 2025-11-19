package com.ljh.sideproj.common.exception;

import com.ljh.sideproj.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 의도적으로 발생시킨 예외 처리 (BusinessException)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[Business Exception] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 혹은 상황에 따라 status 조정 가능
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    // 2. 예상치 못한 나머지 모든 예외 처리 (NullPointer 등)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Unhandled Exception]", e); // 서버 로그에는 에러 스택 남김
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}