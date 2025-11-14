package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.Notification;
import com.ljh.sideproj.dto.UserAlert;
import com.ljh.sideproj.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림 API", description = "알림 관리 API")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "내 알림 조회", description = "로그인한 회원님의 모든 알림 조회")
    @GetMapping
    public ResponseEntity<?> getMyNotifications(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이필요합니다"));
        }
        Long userId = (Long) auth.getDetails();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "읽지 않은 알림 조회")
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("notifications", notifications, "count", count));
    }

    @Operation(summary = "알림 읽음 처리")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "모든 알림을 읽음 처리했습니다."));
    }

    @Operation(summary = "알림 설정 생성")
    @PostMapping("/alerts")
    public ResponseEntity<?> createAlert(@RequestBody UserAlert alert, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        alert.setUserId(userId);
        notificationService.createAlert(alert);
        return ResponseEntity.ok(Map.of("success", true, "message", "알림 설정이 생성되었습니다."));
    }

    @Operation(summary = "내 알림 설정 조회")
    @GetMapping("/alerts")
    public ResponseEntity<?> getMyAlerts(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        List<UserAlert> alerts = notificationService.getUserAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @Operation(summary = "알림 설정 수정")
    @PutMapping("/alerts/{id}")
    public ResponseEntity<?> updateAlert(@PathVariable Long id, @RequestBody UserAlert alert) {
        alert.setAlertId(id);
        notificationService.updateAlert(alert);
        return ResponseEntity.ok(Map.of("success", true, "message", "알림 설정이 수정되었습니다."));
    }

    @Operation(summary = "알림 설정 삭제")
    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        notificationService.deleteAlert(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "알림 설정이 삭제되었습니다."));
    }

    @Operation(summary = "알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "알림을 삭제했습니다."));
    }

    @Operation(summary = "모든 알림 삭제")
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotifications(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "모든 알림을 삭제했습니다."));
    }

    @GetMapping("/test")
    public ResponseEntity<?> createTestNotification(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = (Long) auth.getDetails();
        notificationService.createNotification(userId, "TEST-001", "TEST", "테스트 알림입니다!!!");
        return ResponseEntity.ok(Map.of("success", true, "message", "테스트 알림이 생성되었습니다!!!!"));
    }
}
