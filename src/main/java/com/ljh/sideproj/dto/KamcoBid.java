package com.ljh.sideproj.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KamcoBid {
    private Long id;
    private String pbctNo;
    private String cltrNm;
    private String sido;
    private String sgk;
    private String emd;
    private Long minBidPrc;
    private Long apslAsesAvgAmt;
    private String pbctBgnDt;
    private String pbctEndDt;
    private String dpslMtdCd;
    private String ctgrHirkId;
    private String ctgrHirkIdMid;
    private Long goodsPriceFrom;
    private Long goodsPriceTo;
    private Long openPriceFrom;
    private Long openPriceTo;
    private String cltrMnmtNo;
    private String saleType;
    private String detailAddress;
    private Long appraisalValue;
    private Long depositAmount;
    private String bidMethod;
    private String areaSize;
    private String buildingStructure;
    private String landUse;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}