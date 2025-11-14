package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.Bid;
import com.ljh.sideproj.mapper.BidMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BidService {
    
    @Autowired
    private BidMapper bidMapper;
    
    public void submitBid(Bid bid) {
        bidMapper.insertBid(bid);
    }
    
    public List<Bid> getMyBids(Long userId) {
        return bidMapper.findByUserId(userId);
    }
}
