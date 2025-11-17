package com.ljh.sideproj.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Long bidId;
    private Long userId;        // 누가 입찰했는지
    private String pblncNo;     // 공고 번호
    private String pblncNm;     // 공고명
    private Long bidAmount;     // 입찰 금액
    private LocalDateTime bidDate;
}