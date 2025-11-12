package com.ljh.sideproj.dto;

import lombok.Data;

@Data
public class BidItem {
    private String pblancNo;      // 공고번호
    private String pblancNm;      // 공고명
    private String pblancBeginDt; // 공고시작일
    private String pblancEndDt;   // 공고종료일
    private String opengDt;       // 개찰일시
    private String estmtPrce;     // 추정가격
    private String bidMthdNm;     // 입찰방법
    private String rgstDt;        // 등록일시
}