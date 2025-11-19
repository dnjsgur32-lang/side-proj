package com.ljh.sideproj.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;     // 로그인은 email 기반
    private String password;
}
