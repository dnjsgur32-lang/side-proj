package com.ljh.sideproj.controller;

import com.ljh.sideproj.service.BookmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // ===========================
    // 북마크 추가
    // ===========================
    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestBody Map<String, String> request, Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[북마크 추가 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            String pbctNo = request.get("pbctNo");

            if (pbctNo == null || pbctNo.trim().isEmpty()) {
                log.warn("[북마크 추가 실패] userId={} / pbctNo 누락", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "BOOKMARK_PBCTNO_REQUIRED"
                ));
            }

            bookmarkService.addBookmark(userId, pbctNo.trim());
            log.info("[북마크 추가 성공] userId={}, pbctNo={}", userId, pbctNo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "BOOKMARK_ADD_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[북마크 추가 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "BOOKMARK_ADD_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 북마크 삭제
    // ===========================
    @DeleteMapping("/{pbctNo}")
    public ResponseEntity<?> deleteBookmark(@PathVariable String pbctNo, Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[북마크 삭제 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();

            if (pbctNo == null || pbctNo.trim().isEmpty()) {
                log.warn("[북마크 삭제 실패] userId={} / pbctNo 누락", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "BOOKMARK_PBCTNO_REQUIRED"
                ));
            }

            bookmarkService.removeBookmark(userId, pbctNo.trim());
            log.info("[북마크 삭제 성공] userId={}, pbctNo={}", userId, pbctNo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "BOOKMARK_DELETE_SUCCESS"
            ));
        } catch (Exception e) {
            log.error("[북마크 삭제 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "BOOKMARK_DELETE_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 내 북마크 목록 조회
    // ===========================
    @GetMapping
    public ResponseEntity<?> getBookmarks(Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                log.warn("[북마크 목록 조회 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) auth.getDetails();
            log.info("[북마크 목록 조회 요청] userId={}", userId);

            var bookmarks = bookmarkService.getUserBookmarks(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "BOOKMARK_LIST_OK",
                    "data", bookmarks
            ));
        } catch (Exception e) {
            log.error("[북마크 목록 조회 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "BOOKMARK_LIST_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }
}
