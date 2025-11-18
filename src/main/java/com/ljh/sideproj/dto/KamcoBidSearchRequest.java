package com.ljh.sideproj.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KamcoBidSearchRequest {

    @Schema(description = "페이지 번호", example = "1")
    private int pageNo = 1;          // 기본값 1

    @Schema(description = "페이지당 개수", example = "20")
    private int numOfRows = 20;      // 기본값 20

    @Schema(description = "처분방법 코드")
    private String dpslMtdCd;

    @Schema(description = "대분류 카테고리 ID")
    private String ctgrHirkId;

    @Schema(description = "중분류 카테고리 ID")
    private String ctgrHirkIdMid;

    @Schema(description = "시도", example = "서울특별시")
    private String sido;

    @Schema(description = "시군구", example = "강남구")
    private String sgk;

    @Schema(description = "읍면동", example = "역삼동")
    private String emd;

    @Schema(description = "물건 가격 (최소)")
    private Long goodsPriceFrom;

    @Schema(description = "물건 가격 (최대)")
    private Long goodsPriceTo;

    @Schema(description = "최저 입찰가 (최소)")
    private Long openPriceFrom;

    @Schema(description = "최저 입찰가 (최대)")
    private Long openPriceTo;

    @Schema(description = "물건명", example = "아파트")
    private String cltrNm;

    @Schema(description = "입찰 시작일시(YYYYMMDD 또는 YYYYMMDDHHmmss)")
    private String pbctBegnDtm;

    @Schema(description = "입찰 마감일시(YYYYMMDD 또는 YYYYMMDDHHmmss)")
    private String pbctClsDtm;

    @Schema(description = "물건관리번호")
    private String cltrMnmtNo;
}
