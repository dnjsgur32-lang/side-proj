package com.ljh.sideproj.controller;

import com.ljh.sideproj.common.ApiResponse;
import com.ljh.sideproj.common.exception.BusinessException;
import com.ljh.sideproj.dto.BidRequest;
import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.dto.KamcoBidSearchRequest;
import com.ljh.sideproj.service.BidService;
import com.ljh.sideproj.service.KamcoApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "KAMCO 입찰 API")
public class BidController {

    private final BidService bidService;
    private final KamcoApiService kamcoApiService;

    // ==============================
    // 1) DB 목록 조회 (검색용)
    // ==============================
    @Operation(summary = "DB 입찰 정보 조회 (페이징/필터)")
    @GetMapping("/bids-from-db")
    public ApiResponse<List<KamcoBid>> getBidsFromDb(@ModelAttribute KamcoBidSearchRequest request) {

        List<KamcoBid> bids = kamcoApiService.getBidsFromDb(
                request.getPageNo(),
                request.getNumOfRows(),
                request.getDpslMtdCd(),
                request.getSido(),
                request.getSgk(),
                request.getEmd(),
                request.getCltrNm()
        );

        return ApiResponse.success(bids);
    }

    // ==============================
    // 2) 단일 공고 상세 조회 (상세 페이지 전용)
    // ==============================
    @Operation(summary = "단일 공고 상세 조회")
    @GetMapping("/bid-detail")
    public ApiResponse<KamcoBid> getBidDetail(@RequestParam String pbctNo) {

        if (pbctNo == null || pbctNo.trim().isEmpty()) {
            throw new BusinessException("PBLANC_NO_REQUIRED", "공고번호가 필요합니다.");
        }

        KamcoBid bid = kamcoApiService.getBidDetail(pbctNo);

        if (bid == null) {
            throw new BusinessException("BID_NOT_FOUND", "해당 공고를 찾을 수 없습니다.");
        }

        return ApiResponse.success(bid);
    }

    // ==============================
    // 3) 입찰 신청
    // ==============================
    @Operation(summary = "입찰 신청")
    @PostMapping("/submit-bid")
    public ApiResponse<String> submitBid(@RequestBody BidRequest request, Authentication auth) {

        if (auth == null) {
            // 필터에서 막히겠지만 혹시 모를 방어
            throw new BusinessException("AUTH_REQUIRED", "로그인이 필요합니다.");
        }

        Long userId = (Long) auth.getDetails();
        bidService.submitBid(userId, request);

        return ApiResponse.success("입찰이 성공적으로 접수되었습니다.");
    }

    // ==============================
    // 4) 내 입찰 내역
    // ==============================
    @Operation(summary = "내 입찰 내역")
    @GetMapping("/my-bids-db")
    public ApiResponse<List<com.ljh.sideproj.dto.Bid>> getMyBids(Authentication auth) {

        if (auth == null) {
            throw new BusinessException("AUTH_REQUIRED", "로그인이 필요합니다.");
        }

        Long userId = (Long) auth.getDetails();
        return ApiResponse.success(bidService.getMyBids(userId));
    }

    // ==============================
    // 5) 터보 동기화
    // ==============================
    @Operation(summary = "KAMCO 데이터 동기화")
    @GetMapping("/turbo-sync")
    public ApiResponse<Map<String, Integer>> turboSync() {
        int totalSaved = kamcoApiService.syncAllData(50, 100);
        return ApiResponse.success(Map.of("totalSaved", totalSaved));
    }

    // ==============================
    // 6) 동기화 중지
    // ==============================
    @Operation(summary = "KAMCO 데이터 동기화 중지 요청")
    @PostMapping("/stop-sync")
    public ApiResponse<Void> stopSync() {
        kamcoApiService.requestStopSync();
        return ApiResponse.success();
    }
}
