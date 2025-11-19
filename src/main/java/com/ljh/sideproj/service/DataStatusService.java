package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.DataStatusResponse;
import com.ljh.sideproj.mapper.KamcoBidMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DataStatusService {

    private final KamcoBidMapper kamcoBidMapper; // MyBatis 매퍼 하나만 사용

    public DataStatusResponse getStatus() {

        // 1) 전체 건수
        long totalCount = kamcoBidMapper.countAll();

        // 2) 마지막 동기화 시간: 가장 최신 created_at
        LocalDateTime lastSyncTime = kamcoBidMapper.findLastSyncTime();

        // 3) 상태 메시지 간단 규칙
        String statusMessage = "OK";
        if (totalCount == 0) {
            statusMessage = "NO_DATA";
        }
        if (lastSyncTime == null) {
            statusMessage = "UNKNOWN";
        }

        return DataStatusResponse.builder()
                .totalCount(totalCount)
                .lastSyncTime(lastSyncTime)
                .statusMessage(statusMessage)
                .build();
    }
}
