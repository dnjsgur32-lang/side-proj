package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.Bid;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BidMapper {
    void insertBid(Bid bid);
    List<Bid> findByUserId(Long userId);
}