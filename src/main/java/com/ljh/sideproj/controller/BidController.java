package com.ljh.sideproj.controller;

import com.ljh.sideproj.dto.Bid;
import com.ljh.sideproj.dto.BidRequest;
import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.mapper.BidMapper;
import com.ljh.sideproj.mapper.KamcoBidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "KAMCO 입찰 API", description = "한국자산관리공사 입찰 정보 관련 API")
public class BidController {
    
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
            .build();
    
    @Value("${api.service-key}")
    private String serviceKey;
    
    @Value("${api.base-url}")
    private String baseUrl;
    
    @Autowired
    private BidMapper bidMapper;
    
    @Autowired
    private KamcoBidMapper kamcoBidMapper;
    
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
            StringBuilder uriBuilder = new StringBuilder(baseUrl);
            uriBuilder.append("?serviceKey=").append(serviceKey)
                     .append("&numOfRows=").append(numOfRows)
                     .append("&pageNo=").append(pageNo);
            
            if (dpslMtdCd != null && !dpslMtdCd.trim().isEmpty()) {
                uriBuilder.append("&DPSL_MTD_CD=").append(dpslMtdCd);
            }
            if (ctgrHirkId != null && !ctgrHirkId.trim().isEmpty()) {
                uriBuilder.append("&CTGR_HIRK_ID=").append(ctgrHirkId);
            }
            if (ctgrHirkIdMid != null && !ctgrHirkIdMid.trim().isEmpty()) {
                uriBuilder.append("&CTGR_HIRK_ID_MID=").append(ctgrHirkIdMid);
            }
            if (sido != null && !sido.trim().isEmpty()) {
                uriBuilder.append("&SIDO=").append(sido);
            }
            if (sgk != null && !sgk.trim().isEmpty()) {
                uriBuilder.append("&SGK=").append(sgk);
            }
            if (emd != null && !emd.trim().isEmpty()) {
                uriBuilder.append("&EMD=").append(emd);
            }
            if (goodsPriceFrom != null) {
                uriBuilder.append("&GOODS_PRICE_FROM=").append(goodsPriceFrom);
            }
            if (goodsPriceTo != null) {
                uriBuilder.append("&GOODS_PRICE_TO=").append(goodsPriceTo);
            }
            if (openPriceFrom != null) {
                uriBuilder.append("&OPEN_PRICE_FROM=").append(openPriceFrom);
            }
            if (openPriceTo != null) {
                uriBuilder.append("&OPEN_PRICE_TO=").append(openPriceTo);
            }
            if (cltrNm != null && !cltrNm.trim().isEmpty()) {
                uriBuilder.append("&CLTR_NM=").append(cltrNm);
            }
            if (pbctBegnDtm != null && !pbctBegnDtm.trim().isEmpty()) {
                uriBuilder.append("&PBCT_BEGN_DTM=").append(pbctBegnDtm);
            }
            if (pbctClsDtm != null && !pbctClsDtm.trim().isEmpty()) {
                uriBuilder.append("&PBCT_CLS_DTM=").append(pbctClsDtm);
            }
            if (cltrMnmtNo != null && !cltrMnmtNo.trim().isEmpty()) {
                uriBuilder.append("&CLTR_MNMT_NO=").append(cltrMnmtNo);
            }
            
            return WebClient.create()
                    .get()
                    .uri(uriBuilder.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
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
    
    @GetMapping("/api/test-raw")
    public String testRawApi() {
        try {
            String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=5&pageNo=1";
            String response = WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "<pre>" + response + "</pre>";
        } catch (Exception e) {
            return "<pre>API 호출 오류: " + e.getMessage() + "</pre>";
        }
    }
    
    @GetMapping("/api/test-region-raw")
    public String testRegionRawApi(@RequestParam(defaultValue = "서울특별시") String sido) {
        try {
            String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=5&pageNo=1&SIDO=" + sido;
            System.out.println("테스트 URI: " + uri);
            String response = WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return "<pre>" + response + "</pre>";
        } catch (Exception e) {
            return "<pre>API 호출 오류: " + e.getMessage() + "</pre>";
        }
    }
    
    @GetMapping("/api/simple-sync")
    public ResponseEntity<?> simpleSync(@RequestParam(defaultValue = "10") int count) {
        try {
            String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=" + count + "&pageNo=1";
            System.out.println("API 호출: " + uri);
            
            String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
            System.out.println("응답 길이: " + xmlResponse.length());
            
            int saved = parseAndSaveKamcoDataWithCount(xmlResponse);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "동기화 완료",
                "saved", saved
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "입찰 신청", description = "로그인한 사용자의 입찰 정보를 DB에 저장")
    @ApiResponse(responseCode = "200", description = "입찰 신청 성공")
    @PostMapping("/api/submit-bid")
    public ResponseEntity<?> submitBidToDb(@RequestBody BidRequest request, Authentication authentication){
        try {
            // 로그인 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
            }
            
            Long userId = (Long) authentication.getDetails();
            
            // 입력 데이터 검증
            if (request.getPblancNo() == null || request.getPblancNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "공고번호가 필요합니다"));
            }
            if (request.getBidAmount() == null || request.getBidAmount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "입찰금액이 필요합니다"));
            }
            
            // BidRequest를 Bid 엔티티로 변환
            Bid bid = new Bid();
            bid.setUserId(userId); // 로그인한 사용자 ID 사용
            bid.setPblncNo(request.getPblancNo().trim());
            bid.setPblncNm(request.getPblancNm() != null ? request.getPblancNm().trim() : "입찰 신청");
            bid.setBidAmount(request.getBidAmount());
            
            bidMapper.insertBid(bid);
            
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
            List<Bid> bids = bidMapper.findByUserId(userId);
            
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "내 입찰 내역 조회 중 오류가 발생했습니다"));
        }
    }
    
    // DB 연결 테스트
    @GetMapping("/api/test-db")
    public ResponseEntity<?> testDb() {
        try {
            List<KamcoBid> result = kamcoBidMapper.findWithFilters(null, null, null, null, null, 0, 5);
            return ResponseEntity.ok().body(Map.of("message", "DB 연결 성공", "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "DB 연결 실패: " + e.getMessage()));
        }
    }
    
    // 데이터 상태 확인
    @Operation(summary = "데이터 상태 확인", description = "현재 DB에 저장된 KAMCO 데이터 상태 조회")
    @ApiResponse(responseCode = "200", description = "상태 조회 성공")
    @Cacheable(value = "dataStatus", key = "'status'")
    @GetMapping("/api/data-status")
    public ResponseEntity<?> getDataStatus() {
        try {
            // 전체 데이터 개수
            List<KamcoBid> allData = kamcoBidMapper.findWithFilters(null, null, null, null, null, 0, 1000);
            
            // 최근 5개 데이터
            List<KamcoBid> recentData = kamcoBidMapper.findWithFilters(null, null, null, null, null, 0, 5);
            
            return ResponseEntity.ok().body(Map.of(
                "totalCount", allData.size(),
                "recentData", recentData,
                "message", "현재 DB에 " + allData.size() + "개의 KAMCO 데이터가 있습니다"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 빠른 대량 동기화 (여러 페이지 한번에)
    @Operation(summary = "터보 동기화", description = "KAMCO API에서 대량 데이터를 빠르게 동기화")
    @ApiResponse(responseCode = "200", description = "동기화 성공")
    @CacheEvict(value = {"bidsList", "dataStatus"}, allEntries = true)
    @GetMapping("/api/turbo-sync")
    public ResponseEntity<?> turboSync() {
        try {
            int totalSaved = 0;
            
            for (int i = 1; i <= 10; i++) {
                try {
                    String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=" + i;
                    String xmlResponse = webClient
                            .get()
                            .uri(uri)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    
                    int savedCount = parseAndSaveKamcoDataWithCount(xmlResponse);
                    totalSaved += savedCount;
                    
                    System.out.println("페이지 " + i + " 완료: " + savedCount + "개 저장");
                    
                    if (savedCount == 0) break;
                    
                } catch (Exception e) {
                    System.out.println("페이지 " + i + " 오류: " + e.getMessage());
                    continue;
                }
            }
            
            return ResponseEntity.ok().body(Map.of(
                "message", "터보 동기화 완료!",
                "totalSaved", totalSaved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // KAMCO API 데이터를 DB에 저장하고 조회
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
        int offset = (pageNo - 1) * numOfRows;
        return kamcoBidMapper.findWithFilters(sido, sgk, emd, cltrNm, dpslMtdCd, offset, numOfRows);
    }
    
    // KAMCO API에서 데이터를 가져와서 DB에 저장 (브라우저 테스트용)
    @GetMapping("/api/sync-kamco-data-get")
    public ResponseEntity<?> syncKamcoDataGet() {
        return syncKamcoData();
    }
    
    // 특정 페이지 데이터 동기화
    @GetMapping("/api/sync-page/{pageNo}")
    public ResponseEntity<?> syncSpecificPage(@PathVariable int pageNo) {
        try {
            String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=" + pageNo;
            String xmlResponse = WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            int savedCount = parseAndSaveKamcoDataWithCount(xmlResponse);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "페이지 " + pageNo + " 동기화 완료",
                "pageNo", pageNo,
                "savedCount", savedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // KAMCO API에서 전체 데이터를 가져와서 DB에 저장
    @GetMapping("/api/sync-all-kamco-data")
    public ResponseEntity<?> syncAllKamcoData() {
        try {
            int totalSaved = 0;
            int pageNo = 1;
            int numOfRows = 50; // 한 번에 50개씩 (버퍼 크기 제한 해결)
            
            while (true) {
                // KAMCO API 호출 (전역 WebClient 사용)
                String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=" + numOfRows + "&pageNo=" + pageNo;
                String xmlResponse = webClient
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                // XML 파싱 및 데이터 개수 확인
                int savedCount = parseAndSaveKamcoDataWithCount(xmlResponse);
                totalSaved += savedCount;
                
                // 데이터가 없으면 종료
                if (savedCount == 0) {
                    break;
                }
                
                pageNo++;
                
                // 최대 100페이지까지 (50개씩 100페이지 = 5000개)
                if (pageNo > 100) {
                    break;
                }
                
                // 진행상황 로그
                System.out.println("페이지 " + pageNo + " 완료, 저장된 데이터: " + savedCount + "개");
            }
            
            return ResponseEntity.ok().body(Map.of(
                "message", "KAMCO 전체 데이터 동기화 완료",
                "totalSaved", totalSaved,
                "pages", pageNo - 1,
                "note", "50개씩 " + (pageNo - 1) + "페이지 처리 완료"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // KAMCO API에서 데이터를 가져와서 DB에 저장
    @PostMapping("/api/sync-kamco-data")
    public ResponseEntity<?> syncKamcoData() {
        try {
            // KAMCO API 호출
            String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=1";
            String xmlResponse = WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // XML 파싱 및 DB 저장
            parseAndSaveKamcoData(xmlResponse);
            
            return ResponseEntity.ok().body(Map.of("message", "KAMCO 데이터 동기화 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private void parseAndSaveKamcoData(String xmlResponse) {
        parseAndSaveKamcoDataWithCount(xmlResponse);
    }
    
    private int parseAndSaveKamcoDataWithCount(String xmlResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            
            NodeList items = doc.getElementsByTagName("item");
            
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                
                try {
                    KamcoBid kamcoBid = new KamcoBid();
                    String pbctNo = truncateString(getElementText(item, "PBCT_NO"), 50);
                    
                    // pbct_no가 비어있으면 건너뛰기
                    if (pbctNo == null || pbctNo.trim().isEmpty()) {
                        continue;
                    }
                    
                    kamcoBid.setPbctNo(pbctNo);
                    kamcoBid.setCltrNm(truncateString(getElementText(item, "CLTR_NM"), 500));
                    kamcoBid.setSido(truncateString(getElementText(item, "SIDO"), 50));
                    kamcoBid.setSgk(truncateString(getElementText(item, "SGK"), 50));
                    kamcoBid.setEmd(truncateString(getElementText(item, "EMD"), 50));
                    kamcoBid.setMinBidPrc(parseLong(getElementText(item, "MIN_BID_PRC")));
                    kamcoBid.setApslAsesAvgAmt(parseLong(getElementText(item, "APSL_ASES_AVG_AMT")));
                    kamcoBid.setPbctBgnDt(truncateString(getElementText(item, "PBCT_BGN_DT"), 20));
                    kamcoBid.setPbctEndDt(truncateString(getElementText(item, "PBCT_END_DT"), 20));
                    kamcoBid.setDpslMtdCd(truncateString(getElementText(item, "DPSL_MTD_CD"), 10));
                    kamcoBid.setCtgrHirkId(truncateString(getElementText(item, "CTGR_HIRK_ID"), 20));
                    kamcoBid.setCtgrHirkIdMid(truncateString(getElementText(item, "CTGR_HIRK_ID_MID"), 20));
                    kamcoBid.setCltrMnmtNo(truncateString(getElementText(item, "CLTR_MNMT_NO"), 50));
                    kamcoBid.setSaleType(truncateString(getElementText(item, "SALE_TYPE"), 20));
                    kamcoBid.setDetailAddress(truncateString(getElementText(item, "DETAIL_ADDR"), 500));
                    kamcoBid.setAppraisalValue(parseLong(getElementText(item, "APPRAISAL_VALUE")));
                    kamcoBid.setDepositAmount(parseLong(getElementText(item, "DEPOSIT_AMT")));
                    kamcoBid.setBidMethod(truncateString(getElementText(item, "BID_METHOD"), 50));
                    kamcoBid.setAreaSize(truncateString(getElementText(item, "AREA_SIZE"), 100));
                    kamcoBid.setBuildingStructure(truncateString(getElementText(item, "BUILDING_STRUCTURE"), 100));
                    kamcoBid.setLandUse(truncateString(getElementText(item, "LAND_USE"), 100));
                    kamcoBid.setRemarks(truncateString(getElementText(item, "REMARKS"), 1000));
                    
                    kamcoBidMapper.insertOrUpdateKamcoBid(kamcoBid);
                } catch (Exception e) {
                    System.out.println("데이터 저장 오류 (건너뛰기): " + e.getMessage());
                    continue; // 오류 발생시 다음 데이터로
                }
            }
            
            return items.getLength();
        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 오류: " + e.getMessage());
        }
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : "";
    }
    
    private Long parseLong(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Long.parseLong(value.trim()) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    private String truncateString(String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) return "";
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
    
    private int parseAndSaveKamcoDataByRegion(String xmlResponse, String targetSido, int maxCount) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            
            NodeList items = doc.getElementsByTagName("item");
            int savedCount = 0;
            
            for (int i = 0; i < items.getLength() && savedCount < maxCount; i++) {
                Element item = (Element) items.item(i);
                
                try {
                    String cltrNm = getElementText(item, "CLTR_NM");
                    String sido = extractSidoFromAddress(cltrNm);
                    
                    if (!targetSido.equals(sido)) continue;
                    
                    KamcoBid kamcoBid = new KamcoBid();
                    String pbctNo = truncateString(getElementText(item, "PBCT_NO"), 50);
                    
                    if (pbctNo == null || pbctNo.trim().isEmpty()) continue;
                    
                    kamcoBid.setPbctNo(pbctNo);
                    kamcoBid.setCltrNm(truncateString(getElementText(item, "CLTR_NM"), 500));
                    kamcoBid.setSido(truncateString(sido, 50));
                    kamcoBid.setSgk(truncateString(getElementText(item, "SGK"), 50));
                    kamcoBid.setEmd(truncateString(getElementText(item, "EMD"), 50));
                    kamcoBid.setMinBidPrc(parseLong(getElementText(item, "MIN_BID_PRC")));
                    kamcoBid.setApslAsesAvgAmt(parseLong(getElementText(item, "APSL_ASES_AVG_AMT")));
                    kamcoBid.setPbctBgnDt(truncateString(getElementText(item, "PBCT_BGN_DT"), 20));
                    kamcoBid.setPbctEndDt(truncateString(getElementText(item, "PBCT_END_DT"), 20));
                    kamcoBid.setDpslMtdCd(truncateString(getElementText(item, "DPSL_MTD_CD"), 10));
                    kamcoBid.setCtgrHirkId(truncateString(getElementText(item, "CTGR_HIRK_ID"), 20));
                    kamcoBid.setCtgrHirkIdMid(truncateString(getElementText(item, "CTGR_HIRK_ID_MID"), 20));
                    kamcoBid.setCltrMnmtNo(truncateString(getElementText(item, "CLTR_MNMT_NO"), 50));
                    
                    kamcoBidMapper.insertOrUpdateKamcoBid(kamcoBid);
                    savedCount++;
                } catch (Exception e) {
                    continue;
                }
            }
            
            return savedCount;
        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 오류: " + e.getMessage());
        }
    }
    
    // 테스트용 간단한 입찰 API
    @Operation(summary = "테스트 입찰", description = "간단한 입찰 테스트")
    @GetMapping("/api/test-bid")
    public ResponseEntity<?> testBid() {
        try {
            Bid bid = new Bid();
            bid.setUserId(1L);
            bid.setPblncNo("TEST-" + System.currentTimeMillis());
            bid.setPblncNm("테스트 입찰");
            bid.setBidAmount(1000000L);
            
            bidMapper.insertBid(bid);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "테스트 입찰 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "테스트 입찰 실패: " + e.getMessage()
            ));
        }
    }
    
    // 지역별 조건 동기화 (전체 데이터 가져온 후 필터링)
    @Operation(summary = "지역별 조건 동기화", description = "특정 지역의 데이터를 지정한 개수만큼 동기화")
    @GetMapping("/api/sync-by-region")
    public ResponseEntity<?> syncByRegion(
            @RequestParam String sido,
            @RequestParam(defaultValue = "1000") int maxCount) {
        try {
            int totalSaved = 0;
            int pageNo = 1;
            
            while (totalSaved < maxCount) {
                String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=" + pageNo;
                
                String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                int savedCount = parseAndSaveKamcoDataByRegion(xmlResponse, sido, maxCount - totalSaved);
                
                if (savedCount == 0) break;
                
                totalSaved += savedCount;
                pageNo++;
                
                System.out.println(sido + " - 페이지 " + pageNo + ": " + savedCount + "개 저장 (총 " + totalSaved + "개)");
                
                if (pageNo > 50) break;
            }
            
            return ResponseEntity.ok().body(Map.of(
                "message", sido + " 동기화 완료",
                "sido", sido,
                "totalSaved", totalSaved
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private String extractSidoFromAddress(String address) {
        if (address == null || address.isEmpty()) return "";
        
        String[] regions = {
            "서울특별시", "부산광역시", "대구광역시", "인천광역시",
            "광주광역시", "대전광역시", "울산광역시", "세종특별자치시",
            "경기도", "강원도", "충청북도", "충청남도",
            "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
        };
        
        for (String region : regions) {
            if (address.contains(region)) {
                return region;
            }
        }
        
        return "";
    }
    
    // 전국 주요 도시 일괄 동기화
    @Operation(summary = "전국 주요 도시 일괄 동기화", description = "각 시도별로 지정한 개수만큼 데이터 동기화")
    @GetMapping("/api/sync-all-regions")
    public ResponseEntity<?> syncAllRegions(@RequestParam(defaultValue = "1000") int countPerRegion) {
        String[] regions = {
            "서울특별시", "부산광역시", "대구광역시", "인천광역시",
            "광주광역시", "대전광역시", "울산광역시", "세종특별자치시",
            "경기도", "강원도", "충청북도", "충청남도",
            "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
        };
        
        Map<String, Integer> results = new HashMap<>();
        int grandTotal = 0;
        
        for (String sido : regions) {
            try {
                int totalSaved = 0;
                int pageNo = 1;
                
                while (totalSaved < countPerRegion) {
                    String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=" + pageNo;
                    
                    String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                    int savedCount = parseAndSaveKamcoDataByRegion(xmlResponse, sido, countPerRegion - totalSaved);
                    
                    if (savedCount == 0) break;
                    
                    totalSaved += savedCount;
                    pageNo++;
                    
                    if (pageNo > 50) break;
                }
                
                results.put(sido, totalSaved);
                grandTotal += totalSaved;
                System.out.println(sido + " 완료: " + totalSaved + "개");
                
            } catch (Exception e) {
                results.put(sido, 0);
                System.out.println(sido + " 오류: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok().body(Map.of(
            "message", "전국 동기화 완료",
            "grandTotal", grandTotal,
            "results", results
        ));
    }
}