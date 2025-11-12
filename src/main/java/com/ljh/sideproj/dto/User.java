package com.ljh.sideproj.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}