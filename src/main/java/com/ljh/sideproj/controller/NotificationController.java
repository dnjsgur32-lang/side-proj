package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.Notification;
import com.ljh.sideproj.dto.UserAlert;
import com.ljh.sideproj.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림 API", description = "알림 관리 API")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // ===========================
    // 내 알림 조회
    // ===========================
    @Operation(summary = "내 알림 조회", description = "로그인한 회원님의 모든 알림 조회")
    @GetMapping
    public ResponseEntity<?> getMyNotifications(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[알림 조회 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[알림 조회 요청] userId={}", userId);

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            log.info("[알림 조회 성공] userId={}, count={}", userId, notifications.size());

            // 기존처럼 리스트 그대로 반환 (형태 유지, 메시지 없음)
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("[알림 조회 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "NOTIFICATION_LIST_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 읽지 않은 알림 조회
    // ===========================
    @Operation(summary = "읽지 않은 알림 조회")
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[미읽음 알림 조회 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[미읽음 알림 조회 요청] userId={}", userId);

            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            int count = notificationService.getUnreadCount(userId);

            log.info("[미읽음 알림 조회 성공] userId={}, count={}", userId, count);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "UNREAD_LIST_OK",
                    "notifications", notifications,
                    "count", count
            ));
        } catch (Exception e) {
            log.error("[미읽음 알림 조회 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "UNREAD_LIST_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 단일 알림 읽음 처리
    // ===========================
    @Operation(summary = "알림 읽음 처리")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            log.info("[알림 읽음 처리 요청] notificationId={}", id);
            notificationService.markAsRead(id);
            log.info("[알림 읽음 처리 성공] notificationId={}", id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "NOTIFICATION_READ_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[알림 읽음 처리 오류] notificationId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "NOTIFICATION_READ_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 모든 알림 읽음 처리
    // ===========================
    @Operation(summary = "모든 알림 읽음 처리")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[전체 알림 읽음 처리 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[전체 알림 읽음 처리 요청] userId={}", userId);

            notificationService.markAllAsRead(userId);
            log.info("[전체 알림 읽음 처리 성공] userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "NOTIFICATION_READ_ALL_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[전체 알림 읽음 처리 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "NOTIFICATION_READ_ALL_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 알림 설정 생성
    // ===========================
    @Operation(summary = "알림 설정 생성")
    @PostMapping("/alerts")
    public ResponseEntity<?> createAlert(@RequestBody UserAlert alert, Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[알림 설정 생성 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            alert.setUserId(userId);

            log.info("[알림 설정 생성 요청] userId={}, alert={}", userId, alert);

            notificationService.createAlert(alert);

            log.info("[알림 설정 생성 성공] userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "ALERT_CREATE_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[알림 설정 생성 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "ALERT_CREATE_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 내 알림 설정 조회
    // ===========================
    @Operation(summary = "내 알림 설정 조회")
    @GetMapping("/alerts")
    public ResponseEntity<?> getMyAlerts(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[알림 설정 조회 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[알림 설정 조회 요청] userId={}", userId);

            List<UserAlert> alerts = notificationService.getUserAlerts(userId);
            log.info("[알림 설정 조회 성공] userId={}, count={}", userId, alerts.size());

            // 기존처럼 리스트 그대로 반환 (형태 유지, 메시지 없음)
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("[알림 설정 조회 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "ALERT_LIST_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 알림 설정 수정
    // ===========================
    @Operation(summary = "알림 설정 수정")
    @PutMapping("/alerts/{id}")
    public ResponseEntity<?> updateAlert(@PathVariable Long id, @RequestBody UserAlert alert) {
        try {
            alert.setAlertId(id);
            log.info("[알림 설정 수정 요청] alertId={}, alert={}", id, alert);
            notificationService.updateAlert(alert);
            log.info("[알림 설정 수정 성공] alertId={}", id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "ALERT_UPDATE_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[알림 설정 수정 오류] alertId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "ALERT_UPDATE_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 알림 설정 삭제
    // ===========================
    @Operation(summary = "알림 설정 삭제")
    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        try {
            log.info("[알림 설정 삭제 요청] alertId={}", id);
            notificationService.deleteAlert(id);
            log.info("[알림 설정 삭제 성공] alertId={}", id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "ALERT_DELETE_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[알림 설정 삭제 오류] alertId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "ALERT_DELETE_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 알림 삭제 (단일)
    // ===========================
    @Operation(summary = "알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            log.info("[알림 삭제 요청] notificationId={}", id);
            notificationService.deleteNotification(id);
            log.info("[알림 삭제 성공] notificationId={}", id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "NOTIFICATION_DELETE_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[알림 삭제 오류] notificationId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "NOTIFICATION_DELETE_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 모든 알림 삭제
    // ===========================
    @Operation(summary = "모든 알림 삭제")
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotifications(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[전체 알림 삭제 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[전체 알림 삭제 요청] userId={}", userId);

            notificationService.deleteAllNotifications(userId);

            log.info("[전체 알림 삭제 성공] userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "NOTIFICATION_DELETE_ALL_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[전체 알림 삭제 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "NOTIFICATION_DELETE_ALL_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 테스트 알림 생성
    // ===========================
    @GetMapping("/test")
    public ResponseEntity<?> createTestNotification(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[테스트 알림 생성 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[테스트 알림 생성 요청] userId={}", userId);

            notificationService.createNotification(userId, "TEST-001", "TEST", "테스트 알림입니다!!!");
            log.info("[테스트 알림 생성 성공] userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "TEST_NOTIFICATION_CREATED"
            ));
        } catch (Exception e) {
            log.error("[테스트 알림 생성 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "TEST_NOTIFICATION_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }
}
