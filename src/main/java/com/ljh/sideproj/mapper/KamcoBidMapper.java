package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.KamcoBid;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KamcoBidMapper {
    void insertKamcoBid (KamcoBid kamcoBid);

    void insertOrUpdateKamcoBid (KamcoBid kamcoBid);

    List<KamcoBid> findAll ();

    KamcoBid findByPbctNo (String pbctNo);

    List<KamcoBid> findWithFilters (String sido, String sgk, String emd,
                                    String cltrNm, String dpslMtdCd, int offset, int limit);

    int countWithFilters (String sido, String sgk,
                          String emd, String cltrNm, String dpslMtdCd);
}