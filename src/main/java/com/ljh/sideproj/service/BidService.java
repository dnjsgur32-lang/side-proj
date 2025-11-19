package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.Bid;
import com.ljh.sideproj.dto.BidRequest;
import com.ljh.sideproj.mapper.BidMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidMapper bidMapper;

    /**
     * 입찰 신청 처리
     * - 컨트롤러에서 받은 userId + BidRequest -> Bid 엔티티로 변환해서 DB에 저장
     */
    public void submitBid(Long userId, BidRequest request) {
        Bid bid = new Bid();

        // 로그인한 유저
        bid.setUserId(userId);

        // 공고 정보
        bid.setPblncNo(request.getPblancNo());  // 철자 다름 주의
        bid.setPblncNm(request.getPblancNm());

        // 입찰 금액
        bid.setBidAmount(request.getBidAmount());

        // bidDate 는 DB 컬럼이 DEFAULT CURRENT_TIMESTAMP 라면 굳이 세팅 안해도 됨

        bidMapper.insertBid(bid);
    }

    /**
     * 내 입찰 내역 조회
     */
    public List<Bid> getMyBids(Long userId) {
        return bidMapper.findByUserId(userId);
    }

    /**
     * (옵션) 공고번호 기준 입찰 내역
     */
    public List<Bid> getBidsByPblncNo(String pblncNo) {
        return bidMapper.findByPblncNo(pblncNo);
    }
}
