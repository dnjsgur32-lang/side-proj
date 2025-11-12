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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}