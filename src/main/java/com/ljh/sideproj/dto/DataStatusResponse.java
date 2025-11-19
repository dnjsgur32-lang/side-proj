package com.ljh.sideproj.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DataStatusResponse {

    // 전체 레코드 수
    private final long totalCount;

    // 마지막 동기화 시간
    private final LocalDateTime lastSyncTime;

    // 상태 메시지 (OK, NO_DATA 등)
    private final String statusMessage;
}
