package com.ljh.sideproj.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Long bidId;
    private Long userId;
    private String pblncNo;
    private String pblncNm;
    private Long bidAmount;
    private LocalDateTime bidDate;
}