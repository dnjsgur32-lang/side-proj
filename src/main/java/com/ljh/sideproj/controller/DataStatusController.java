package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.DataStatusResponse;
import com.ljh.sideproj.service.DataStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataStatusController {

    // 1) 서비스 주입 (생성자 자동 생성)
    private final DataStatusService dataStatusService;

    // 2) GET /api/data-status
    @GetMapping("/data-status")
    public ResponseEntity<DataStatusResponse> getDataStatus() {
        log.info("GET /api/data-status 호출됨");

        // 3) 서비스에서 상태 정보 조회
        DataStatusResponse status = dataStatusService.getStatus();

        // 4) JSON 형태로 응답
        return ResponseEntity.ok(status);
    }
}
