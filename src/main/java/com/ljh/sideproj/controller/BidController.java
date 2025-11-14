package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.Bid;
import com.ljh.sideproj.dto.BidRequest;
import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.service.BidService;
import com.ljh.sideproj.service.KamcoApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "KAMCO 입찰 API", description = "한국자산관리공사 입찰 정보 관련 API")
public class BidController {
    
    @Autowired
    private BidService bidService;
    
    @Autowired
    private KamcoApiService kamcoApiService;
    
    private List<BidRequest> bidRequests = new ArrayList<>();
    
    @GetMapping("/api/bids")
    public String getBids(@RequestParam(defaultValue = "1") int pageNo,
                         @RequestParam(defaultValue = "20") int numOfRows,
                         @RequestParam(required = false) String dpslMtdCd,
                         @RequestParam(required = false) String ctgrHirkId,
                         @RequestParam(required = false) String ctgrHirkIdMid,
                         @RequestParam(required = false) String sido,
                         @RequestParam(required = false) String sgk,
                         @RequestParam(required = false) String emd,
                         @RequestParam(required = false) Long goodsPriceFrom,
                         @RequestParam(required = false) Long goodsPriceTo,
                         @RequestParam(required = false) Long openPriceFrom,
                         @RequestParam(required = false) Long openPriceTo,
                         @RequestParam(required = false) String cltrNm,
                         @RequestParam(required = false) String pbctBegnDtm,
                         @RequestParam(required = false) String pbctClsDtm,
                         @RequestParam(required = false) String cltrMnmtNo) {
        try {
            return kamcoApiService.fetchBidsFromApi(pageNo, numOfRows, dpslMtdCd, ctgrHirkId,
                    ctgrHirkIdMid, sido, sgk, emd, goodsPriceFrom, goodsPriceTo,
                    openPriceFrom, openPriceTo, cltrNm, pbctBegnDtm, pbctClsDtm, cltrMnmtNo);
        } catch (Exception e) {
            return "{\"error\": \"API 호출 오류: " + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/api/bid")
    public Map<String, Object> submitBid(@RequestBody BidRequest bidRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            bidRequests.add(bidRequest);
            response.put("success", true);
            response.put("message", "입찰 신청이 성공적으로 접수되었습니다.");
            response.put("bidId", bidRequests.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "입찰 신청 중 오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/api/my-bids")
    public List<BidRequest> getMyBids() {
        return bidRequests;
    }
    
    @GetMapping("/api/simple-sync")
    public ResponseEntity<?> simpleSync(@RequestParam(defaultValue = "10") int count) {
        try {
            int saved = kamcoApiService.syncData(count);
            return ResponseEntity.ok().body(Map.of("message", "동기화 완료", "saved", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "입찰 신청", description = "로그인한 사용자의 입찰 정보를 DB에 저장")
    @ApiResponse(responseCode = "200", description = "입찰 신청 성공")
    @PostMapping("/api/submit-bid")
    public ResponseEntity<?> submitBidToDb(@RequestBody BidRequest request, Authentication authentication){
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
            }
            
            Long userId = (Long) authentication.getDetails();
            
            if (request.getPblancNo() == null || request.getPblancNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "공고번호가 필요합니다"));
            }
            if (request.getBidAmount() == null || request.getBidAmount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "입찰금액이 필요합니다"));
            }
            
            Bid bid = new Bid();
            bid.setUserId(userId);
            bid.setPblncNo(request.getPblancNo().trim());
            bid.setPblncNm(request.getPblancNm() != null ? request.getPblancNm().trim() : "입찰 신청");
            bid.setBidAmount(request.getBidAmount());
            
            bidService.submitBid(bid);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "입찰 신청이 성공적으로 접수되었습니다",
                "username", authentication.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "입찰 신청 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "내 입찰 현황", description = "로그인한 사용자의 입찰 내역 조회")
    @GetMapping("/api/my-bids-db")
    public ResponseEntity<?> getMyBidsFromDb(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
            }
            
            Long userId = (Long) authentication.getDetails();
            return ResponseEntity.ok(bidService.getMyBids(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "내 입찰 내역 조회 중 오류가 발생했습니다"));
        }
    }
    
    @Operation(summary = "터보 동기화", description = "KAMCO API에서 대량 데이터를 빠르게 동기화")
    @ApiResponse(responseCode = "200", description = "동기화 성공")
    @CacheEvict(value = {"bidsList", "dataStatus"}, allEntries = true)
    @GetMapping("/api/turbo-sync")
    public ResponseEntity<?> turboSync() {
        try {
            int totalSaved = kamcoApiService.syncAllData(50, 100);
            return ResponseEntity.ok().body(Map.of("message", "터보 동기화 완료!", "totalSaved", totalSaved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(summary = "DB에서 입찰 정보 조회", description = "저장된 KAMCO 입찰 데이터를 필터링 및 페이징하여 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Cacheable(value = "bidsList", key = "#pageNo + '_' + #numOfRows + '_' + #sido + '_' + #sgk + '_' + #emd + '_' + #cltrNm + '_' + #dpslMtdCd")
    @GetMapping("/api/bids-from-db")
    public List<KamcoBid> getBidsFromDb(
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "페이지당 개수", example = "20") @RequestParam(defaultValue = "20") int numOfRows,
            @Parameter(description = "처분방법 코드") @RequestParam(required = false) String dpslMtdCd,
            @Parameter(description = "시도", example = "서울특별시") @RequestParam(required = false) String sido,
            @Parameter(description = "시군구", example = "강남구") @RequestParam(required = false) String sgk,
            @Parameter(description = "읍면동", example = "역삼동") @RequestParam(required = false) String emd,
            @Parameter(description = "물건명", example = "아파트") @RequestParam(required = false) String cltrNm) {
        return kamcoApiService.getBidsFromDb(pageNo, numOfRows, dpslMtdCd, sido, sgk, emd, cltrNm);
    }
    
    @PostMapping("/api/sync-kamco-data")
    public ResponseEntity<?> syncKamcoData() {
        try {
            kamcoApiService.syncData(100);
            return ResponseEntity.ok().body(Map.of("message", "KAMCO 데이터 동기화 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/sync-kamco-data-get")
    public ResponseEntity<?> syncKamcoDataGet() {
        return syncKamcoData();
    }
    
    @GetMapping("/api/sync-all-kamco-data")
    public ResponseEntity<?> syncAllKamcoData() {
        try {
            int totalSaved = kamcoApiService.syncAllData(100, 50);
            return ResponseEntity.ok().body(Map.of(
                "message", "KAMCO 전체 데이터 동기화 완료",
                "totalSaved", totalSaved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
