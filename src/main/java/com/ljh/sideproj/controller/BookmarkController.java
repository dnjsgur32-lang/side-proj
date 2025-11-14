package com.ljh.sideproj.controller;

import com.ljh.sideproj.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestBody Map<String, String> request, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long userId = (Long) auth.getDetails();
        String pbctNo = request.get("pbctNo");

        bookmarkService.addBookmark(userId, pbctNo);
        return ResponseEntity.ok(Map.of("success", true, "message", "북마크 추가 완료"));
    }

    @DeleteMapping("/{pbctNo}")
    public ResponseEntity<?> deleteBookmark(@PathVariable String pbctNo, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long userId = (Long) auth.getDetails();
        bookmarkService.removeBookmark(userId, pbctNo);
        return ResponseEntity.ok(Map.of("success", true, "message", "북마크 삭제 완료"));
    }

    @GetMapping
    public ResponseEntity<?> getBookmarks(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        Long userId = (Long) auth.getDetails();
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(userId));
    }
}
