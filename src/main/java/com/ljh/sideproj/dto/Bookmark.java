package com.ljh.sideproj.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Bookmark {
    private Long bookmarkId;
    private Long userId;
    private String pbctNo;
    private LocalDateTime createdAt;
}
