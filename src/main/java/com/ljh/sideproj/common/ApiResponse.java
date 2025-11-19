package com.ljh.sideproj.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    // 성공 시 데이터와 함께 반환
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    // 성공했지만 데이터는 없을 때 (예: 삭제 성공)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
    }

    // 실패 시 반환 (예외 핸들러에서 사용)
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}