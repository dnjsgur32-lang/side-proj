package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.User;
import com.ljh.sideproj.mapper.UserMapper;
import com.ljh.sideproj.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "인증 API", description = "로그인, 회원가입 관련 API")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===========================
    // 회원가입
    // ===========================
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            String phone = request.get("phone");

            // 입력 검증
            if (username == null || username.trim().isEmpty()) {
                log.warn("[회원가입 실패] 사용자명 없음");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "REGISTER_USERNAME_REQUIRED"
                ));
            }
            if (password == null || password.length() < 4) {
                log.warn("[회원가입 실패] 비밀번호 너무 짧음 / username={}", username);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "REGISTER_PASSWORD_TOO_SHORT"
                ));
            }

            // 중복 사용자 확인
            if (userMapper.findByUsername(username) != null) {
                log.warn("[회원가입 실패] 이미 존재하는 사용자명 / username={}", username);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "REGISTER_USERNAME_EXISTS"
                ));
            }

            // 사용자 생성
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phone);

            userMapper.insertUser(user);

            log.info("[회원가입 성공] userId={}, username={}", user.getUserId(), username);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "REGISTER_SUCCESS",
                    "userId", user.getUserId()
            ));
        } catch (Exception e) {
            log.error("[회원가입 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "REGISTER_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 로그인
    // ===========================
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                log.warn("[로그인 실패] 필수 값 누락");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "LOGIN_CREDENTIALS_REQUIRED"
                ));
            }

            // 사용자 확인
            User user = userMapper.findByUsername(username);
            if (user == null) {
                log.warn("[로그인 실패] 존재하지 않는 사용자 / username={}", username);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "LOGIN_USER_NOT_FOUND"
                ));
            }

            // 비밀번호 확인
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("[로그인 실패] 비밀번호 불일치 / username={}", username);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "LOGIN_PASSWORD_MISMATCH"
                ));
            }

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(user.getUsername(), user.getUserId());

            log.info("[로그인 성공] userId={}, username={}", user.getUserId(), username);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "LOGIN_SUCCESS",
                    "token", token,
                    "userId", user.getUserId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            log.error("[로그인 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "LOGIN_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 토큰 검증
    // ===========================
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                log.warn("[토큰 검증 실패] 토큰 없음");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "TOKEN_REQUIRED",
                        "valid", false
                ));
            }

            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                log.info("[토큰 검증 성공] userId={}, username={}", userId, username);

                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "code", "TOKEN_VALID",
                        "valid", true,
                        "username", username,
                        "userId", userId
                ));
            } else {
                log.warn("[토큰 검증 실패] 유효하지 않은 토큰");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "TOKEN_INVALID",
                        "valid", false
                ));
            }
        } catch (Exception e) {
            log.error("[토큰 검증 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "TOKEN_VERIFY_ERROR",
                    "valid", false,
                    "detail", e.getMessage()
            ));
        }
    }
}
