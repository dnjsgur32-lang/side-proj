package com.ljh.sideproj.scheduler;

import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.mapper.KamcoBidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

@Component
public class DataSyncScheduler {
    
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
    
    @Value("${api.service-key}")
    private String serviceKey;
    
    @Value("${api.base-url}")
    private String baseUrl;
    
    @Autowired
    private KamcoBidMapper kamcoBidMapper;
    
    @Scheduled(cron = "0 0 2 * * *")
    public void autoSyncData() {
        try {
            System.out.println("자동 데이터 수집 시작...");
            
            if (serviceKey == null || serviceKey.isEmpty() || baseUrl == null || baseUrl.isEmpty()) {
                System.err.println("자동 데이터 수집 실패: API 키 또는 URL이 설정되지 않음");
                return;
            }
            
            int totalSaved = 0;
            
            for (int pageNo = 1; pageNo <= 20; pageNo++) {
                try {
                    String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=100&pageNo=" + pageNo;
                    String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                    
                    if (xmlResponse == null || xmlResponse.isEmpty()) {
                        System.out.println("페이지 " + pageNo + ": 빈 응답");
                        break;
                    }
                    
                    int savedCount = parseAndSaveData(xmlResponse);
                    totalSaved += savedCount;
                    
                    if (savedCount == 0) break;
                    
                    System.out.println("페이지 " + pageNo + " 완료: " + savedCount + "개 저장");
                } catch (Exception e) {
                    System.err.println("페이지 " + pageNo + " 처리 오류: " + e.getMessage());
                    continue;
                }
            }
            
            System.out.println("자동 데이터 수집 완료: 총 " + totalSaved + "개");
        } catch (Exception e) {
            System.err.println("자동 데이터 수집 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int parseAndSaveData(String xmlResponse) {
        try {
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                return 0;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            
            NodeList items = doc.getElementsByTagName("item");
            int successCount = 0;
            
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                
                try {
                    KamcoBid kamcoBid = new KamcoBid();
                    String pbctNo = truncateString(getElementText(item, "PBCT_NO"), 50);
                    
                    if (pbctNo == null || pbctNo.trim().isEmpty()) continue;
                    
                    String cltrNm = getElementText(item, "CLTR_NM");
                    String sido = extractSidoFromAddress(cltrNm);
                    
                    kamcoBid.setPbctNo(pbctNo);
                    kamcoBid.setCltrNm(truncateString(cltrNm, 500));
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
                    successCount++;
                } catch (Exception e) {
                    System.err.println("항목 처리 오류: " + e.getMessage());
                    continue;
                }
            }
            
            return successCount;
        } catch (Exception e) {
            System.err.println("파싱 오류: " + e.getMessage());
            e.printStackTrace();
            return 0;
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
}
