package com.ljh.sideproj.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private Long notificationId;
    private Long userId;
    private String pbctNo;
    private String notificationType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
