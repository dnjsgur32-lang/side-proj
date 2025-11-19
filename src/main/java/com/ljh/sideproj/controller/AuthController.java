package com.ljh.sideproj.controller;

import com.ljh.sideproj.common.ApiResponse;
import com.ljh.sideproj.dto.LoginRequest;
import com.ljh.sideproj.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "로그인, 회원가입, 토큰 검증")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ApiResponse<Long> register(@RequestBody Map<String, String> request) {
        Long userId = authService.register(request);
        return ApiResponse.success(userId);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {

        Map<String, String> loginMap = Map.of(
                "email", request.getEmail(),
                "password", request.getPassword()
        );

        Map<String, Object> result = authService.login(loginMap);
        return ApiResponse.success(result);
    }

    @Operation(summary = "토큰 검증")
    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verifyToken(@RequestBody Map<String, String> request) {
        Map<String, Object> result = authService.verifyToken(request.get("token"));
        return ApiResponse.success(result);
    }
}
