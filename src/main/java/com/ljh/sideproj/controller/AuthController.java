package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.User;
import com.ljh.sideproj.mapper.UserMapper;
import com.ljh.sideproj.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                return ResponseEntity.badRequest().body(Map.of("error", "사용자명이 필요합니다"));
            }
            if (password == null || password.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "비밀번호는 4자 이상이어야 합니다"));
            }

            // 중복 사용자 확인
            if (userMapper.findByUsername(username) != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미 존재하는 사용자명입니다"));
            }

            // 사용자 생성
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phone);

            userMapper.insertUser(user);

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다",
                "userId", user.getUserId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자명과 비밀번호가 필요합니다"));
            }

            // 사용자 확인
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 사용자입니다"));
            }

            // 비밀번호 확인
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "비밀번호가 일치하지 않습니다"));
            }

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(user.getUsername(), user.getUserId());

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "로그인 성공",
                "token", token,
                "userId", user.getUserId(),
                "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "로그인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "토큰이 필요합니다"));
            }

            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                return ResponseEntity.ok().body(Map.of(
                    "valid", true,
                    "username", username,
                    "userId", userId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "유효하지 않은 토큰입니다"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "토큰 검증 중 오류가 발생했습니다"));
        }
    }
}