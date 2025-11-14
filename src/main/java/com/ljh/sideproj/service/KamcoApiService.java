package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.mapper.KamcoBidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class KamcoApiService {
    
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
    
    @Value("${api.service-key}")
    private String serviceKey;
    
    @Value("${api.base-url}")
    private String baseUrl;
    
    @Autowired
    private KamcoBidMapper kamcoBidMapper;
    
    public List<KamcoBid> getBidsFromDb(int pageNo, int numOfRows, String dpslMtdCd, 
                                        String sido, String sgk, String emd, String cltrNm) {
        int offset = (pageNo - 1) * numOfRows;
        return kamcoBidMapper.findWithFilters(sido, sgk, emd, cltrNm, dpslMtdCd, offset, numOfRows);
    }
    
    public KamcoBid getBidDetail(String pbctNo) {
        return kamcoBidMapper.findByPbctNo(pbctNo);
    }
    
    public int syncData(int count) {
        String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=" + count + "&pageNo=1";
        String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
        return parseAndSaveKamcoData(xmlResponse);
    }
    
    public String fetchBidsFromApi(int pageNo, int numOfRows, String dpslMtdCd, String ctgrHirkId,
                                   String ctgrHirkIdMid, String sido, String sgk, String emd,
                                   Long goodsPriceFrom, Long goodsPriceTo, Long openPriceFrom,
                                   Long openPriceTo, String cltrNm, String pbctBegnDtm,
                                   String pbctClsDtm, String cltrMnmtNo) {
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
    }
    
    public int syncAllData(int maxPages, int numOfRows) {
        int totalSaved = 0;
        for (int pageNo = 1; pageNo <= maxPages; pageNo++) {
            try {
                String uri = baseUrl + "?serviceKey=" + serviceKey + "&numOfRows=" + numOfRows + "&pageNo=" + pageNo;
                String xmlResponse = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                int savedCount = parseAndSaveKamcoData(xmlResponse);
                totalSaved += savedCount;
                if (savedCount == 0) break;
            } catch (Exception e) {
                System.out.println("페이지 " + pageNo + " 오류: " + e.getMessage());
                continue;
            }
        }
        return totalSaved;
    }
    
    public int parseAndSaveKamcoData(String xmlResponse) {
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
                    continue;
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
}
