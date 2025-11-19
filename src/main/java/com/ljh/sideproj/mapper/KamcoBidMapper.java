package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.KamcoBid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KamcoBidMapper {

    // 🔍 리스트 조회 (이미 있을 것)
    List<KamcoBid> findWithFilters (@Param("sido") String sido,
                                    @Param("sgk") String sgk,
                                    @Param("emd") String emd,
                                    @Param("cltrNm") String cltrNm,
                                    @Param("dpslMtdCd") String dpslMtdCd,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    // 💾 동기화시 사용 (이미 있을 것)
    void insertOrUpdateKamcoBid (KamcoBid kamcoBid);

    // ✅ 단건 상세 조회용 메서드 추가
    KamcoBid findByPbctNo (@Param("pbctNo") String pbctNo);

    long countAll ();

    LocalDateTime findLastSyncTime ();
}
