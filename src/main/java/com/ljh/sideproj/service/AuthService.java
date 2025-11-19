package com.ljh.sideproj.service;

import com.ljh.sideproj.common.exception.BusinessException;
import com.ljh.sideproj.dto.User;
import com.ljh.sideproj.mapper.UserMapper;
import com.ljh.sideproj.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public Long register(Map<String, String> req) {
        String email = req.get("email");
        String username = req.get("username");
        String password = req.get("password");
        String phone = req.get("phone");

        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("REGISTER_EMAIL_REQUIRED", "이메일을 입력해주세요.");
        }
        if (password == null || password.length() < 4) {
            throw new BusinessException("REGISTER_PASSWORD_TOO_SHORT", "비밀번호는 4자 이상이어야 합니다.");
        }
        if (userMapper.findByEmail(email) != null) {
            throw new BusinessException("REGISTER_EMAIL_EXISTS", "이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);

        userMapper.insertUser(user);
        log.info("회원가입 성공: {}", email);

        return user.getUserId();
    }

    public Map<String, Object> login(Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");

        if (email == null || password == null) {
            throw new BusinessException("LOGIN_CREDENTIALS_REQUIRED", "이메일과 비밀번호를 입력해주세요.");
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("LOGIN_USER_NOT_FOUND", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("LOGIN_PASSWORD_MISMATCH", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

        log.info("로그인 성공: {}", email);

        return Map.of(
                "token", token,
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        );
    }

    public Map<String, Object> verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("TOKEN_REQUIRED", "토큰이 필요합니다.");
        }

        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException("TOKEN_INVALID", "유효하지 않은 토큰입니다.");
        }

        return Map.of(
                "valid", true,
                "userId", jwtUtil.getUserIdFromToken(token),
                "email", jwtUtil.getUsernameFromToken(token)
        );
    }
}
