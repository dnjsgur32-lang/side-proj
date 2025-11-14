package com.ljh.sideproj.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAlert {
    private Long alertId;
    private Long userId;
    private String pbctNo;
    private String alertType;
    private Integer alertDaysBefore;
    private Long targetPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
