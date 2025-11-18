package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.Bid;
import com.ljh.sideproj.dto.BidRequest;
import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.dto.KamcoBidSearchRequest;
import com.ljh.sideproj.service.BidService;
import com.ljh.sideproj.service.KamcoApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "KAMCO 입찰 API", description = "한국자산관리공사 입찰 정보 관련 API")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private KamcoApiService kamcoApiService;

    // 메모리 테스트용
    private List<BidRequest> bidRequests = new ArrayList<>();

    // ===========================
    // KAMCO API 직접 조회 (검색 DTO 사용)
    // ===========================
    @GetMapping("/api/bids")
    public String getBids(@ModelAttribute KamcoBidSearchRequest request) {
        try {
            log.info("[KAMCO API 조회] pageNo={}, numOfRows={}, sido={}, sgk={}, emd={}, cltrNm={}",
                    request.getPageNo(), request.getNumOfRows(),
                    request.getSido(), request.getSgk(), request.getEmd(), request.getCltrNm());

            return kamcoApiService.fetchBidsFromApi(
                    request.getPageNo(),
                    request.getNumOfRows(),
                    request.getDpslMtdCd(),
                    request.getCtgrHirkId(),
                    request.getCtgrHirkIdMid(),
                    request.getSido(),
                    request.getSgk(),
                    request.getEmd(),
                    request.getGoodsPriceFrom(),
                    request.getGoodsPriceTo(),
                    request.getOpenPriceFrom(),
                    request.getOpenPriceTo(),
                    request.getCltrNm(),
                    request.getPbctBegnDtm(),
                    request.getPbctClsDtm(),
                    request.getCltrMnmtNo()
            );
        } catch (Exception e) {
            log.error("[KAMCO API 조회 오류] {}", e.getMessage(), e);
            // ✅ 프론트로 나가는 메시지는 영어/코드 위주
            return "{\"success\": false, \"code\": \"API_ERROR\", \"detail\": \"" + e.getMessage() + "\"}";
        }
    }

    // ===========================
    // 메모리 테스트용 입찰 신청
    // ===========================
    @PostMapping("/api/bid")
    public Map<String, Object> submitBid(@RequestBody BidRequest bidRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            bidRequests.add(bidRequest);
            log.info("[메모리 입찰 신청 성공] 현재 총 건수={}", bidRequests.size());

            response.put("success", true);
            response.put("code", "BID_SUBMIT_SUCCESS");
            response.put("bidId", bidRequests.size());
        } catch (Exception e) {
            log.error("[메모리 입찰 신청 오류] {}", e.getMessage(), e);

            response.put("success", false);
            response.put("code", "BID_SUBMIT_ERROR");
            response.put("detail", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/my-bids")
    public List<BidRequest> getMyBids() {
        log.info("[메모리 내 입찰 조회] 총 건수={}", bidRequests.size());
        return bidRequests;
    }

    @GetMapping("/api/simple-sync")
    public ResponseEntity<?> simpleSync(@RequestParam(defaultValue = "10") int count) {
        try {
            log.info("[간단 동기화 시작] count={}", count);
            int saved = kamcoApiService.syncData(count);
            log.info("[간단 동기화 완료] 저장 건수={}", saved);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "SIMPLE_SYNC_DONE",
                    "saved", saved
            ));
        } catch (Exception e) {
            log.error("[간단 동기화 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "SIMPLE_SYNC_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // DB 입찰 신청
    // ===========================
    @Operation(summary = "입찰 신청", description = "로그인한 사용자의 입찰 정보를 DB에 저장")
    @ApiResponse(responseCode = "200", description = "입찰 신청 성공")
    @PostMapping("/api/submit-bid")
    public ResponseEntity<?> submitBidToDb(@RequestBody BidRequest request, Authentication authentication){
        try {
            if (authentication == null || authentication.getName() == null) {
                log.warn("[입찰 신청 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) authentication.getDetails();

            if (request.getPblancNo() == null || request.getPblancNo().trim().isEmpty()) {
                log.warn("[입찰 신청 실패] userId={} / 공고번호 누락", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "PBLANC_NO_REQUIRED"
                ));
            }
            if (request.getBidAmount() == null || request.getBidAmount() <= 0) {
                log.warn("[입찰 신청 실패] userId={} / 입찰금액 누락 또는 0 이하", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", "BID_AMOUNT_REQUIRED"
                ));
            }

            Bid bid = new Bid();
            bid.setUserId(userId);
            bid.setPblncNo(request.getPblancNo().trim());
            // ✅ DB에 저장되는 기본 텍스트는 코드 느낌으로 (프론트에서 한글로 변환 가능)
            bid.setPblncNm(request.getPblancNm() != null ? request.getPblancNm().trim() : "BID_REQUEST");
            bid.setBidAmount(request.getBidAmount());

            bidService.submitBid(bid);

            log.info("[입찰 신청 성공] userId={}, pblancNo={}, amount={}",
                    userId, bid.getPblncNo(), bid.getBidAmount());

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "BID_SUBMIT_SUCCESS",
                    "username", authentication.getName()
            ));
        } catch (Exception e) {
            log.error("[입찰 신청 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "BID_SUBMIT_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // DB 내 입찰 내역 조회
    // ===========================
    @Operation(summary = "내 입찰 현황", description = "로그인한 사용자의 입찰 내역 조회")
    @GetMapping("/api/my-bids-db")
    public ResponseEntity<?> getMyBidsFromDb(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                log.warn("[내 입찰 조회 실패] 인증 정보 없음");
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "code", "AUTH_REQUIRED"
                ));
            }

            Long userId = (Long) authentication.getDetails();
            log.info("[내 입찰 조회 요청] userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", "MY_BIDS_OK",
                    "data", bidService.getMyBids(userId)
            ));
        } catch (Exception e) {
            log.error("[내 입찰 조회 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "MY_BIDS_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // 터보 동기화
    // ===========================
    @Operation(summary = "터보 동기화", description = "KAMCO API에서 대량 데이터를 빠르게 동기화")
    @ApiResponse(responseCode = "200", description = "동기화 성공")
    @CacheEvict(value = {"bidsList", "dataStatus"}, allEntries = true)
    @GetMapping("/api/turbo-sync")
    public ResponseEntity<?> turboSync() {
        try {
            log.info("[터보 동기화 시작]");
            int totalSaved = kamcoApiService.syncAllData(50, 100);
            log.info("[터보 동기화 완료] totalSaved={}", totalSaved);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "TURBO_SYNC_DONE",
                    "totalSaved", totalSaved
            ));
        } catch (Exception e) {
            log.error("[터보 동기화 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "TURBO_SYNC_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    // ===========================
    // DB에서 입찰 정보 조회
    // ===========================
    @Operation(summary = "DB에서 입찰 정보 조회", description = "저장된 KAMCO 입찰 데이터를 필터링 및 페이징하여 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Cacheable(
            value = "bidsList",
            key = "#request.pageNo + '_' + #request.numOfRows + '_' + #request.sido + '_' + #request.sgk + '_' + #request.emd + '_' + #request.cltrNm + '_' + #request.dpslMtdCd"
    )
    @GetMapping("/api/bids-from-db")
    public List<KamcoBid> getBidsFromDb(@ModelAttribute KamcoBidSearchRequest request) {
        log.info("[DB 입찰 조회] pageNo={}, numOfRows={}, sido={}, sgk={}, emd={}, cltrNm={}, dpslMtdCd={}",
                request.getPageNo(), request.getNumOfRows(),
                request.getSido(), request.getSgk(),
                request.getEmd(), request.getCltrNm(), request.getDpslMtdCd());

        return kamcoApiService.getBidsFromDb(
                request.getPageNo(),
                request.getNumOfRows(),
                request.getDpslMtdCd(),
                request.getSido(),
                request.getSgk(),
                request.getEmd(),
                request.getCltrNm()
        );
    }

    // ===========================
    // 싱크 API들
    // ===========================
    @PostMapping("/api/sync-kamco-data")
    public ResponseEntity<?> syncKamcoData() {
        try {
            log.info("[KAMCO 데이터 동기화 시작] 기본 갯수=100");
            kamcoApiService.syncData(100);
            log.info("[KAMCO 데이터 동기화 완료]");

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "SYNC_DATA_DONE"
            ));
        } catch (Exception e) {
            log.error("[KAMCO 데이터 동기화 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "SYNC_DATA_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/sync-kamco-data-get")
    public ResponseEntity<?> syncKamcoDataGet() {
        return syncKamcoData();
    }

    @GetMapping("/api/sync-all-kamco-data")
    public ResponseEntity<?> syncAllKamcoData() {
        try {
            log.info("[KAMCO 전체 데이터 동기화 시작]");
            int totalSaved = kamcoApiService.syncAllData(100, 50);
            log.info("[KAMCO 전체 데이터 동기화 완료] totalSaved={}", totalSaved);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "code", "SYNC_ALL_DATA_DONE",
                    "totalSaved", totalSaved
            ));
        } catch (Exception e) {
            log.error("[KAMCO 전체 데이터 동기화 오류] {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "SYNC_ALL_DATA_ERROR",
                    "detail", e.getMessage()
            ));
        }
    }
}
