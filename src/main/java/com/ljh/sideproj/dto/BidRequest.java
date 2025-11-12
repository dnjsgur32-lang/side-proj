package com.ljh.sideproj.dto;

import lombok.Data;

@Data
public class BidRequest {
    private String pblancNo;    // 공고번호
    private String pblancNm;    // 공고명
    private String bidderName;  // 입찰자명
    private Long bidAmount;     // 입찰금액
    private String phoneNumber; // 연락처
    private String email;       // 이메일
}