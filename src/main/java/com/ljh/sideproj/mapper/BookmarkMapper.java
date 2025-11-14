package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.Bookmark;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookmarkMapper {
    void insertBookmark(Bookmark bookmark);
    List<Bookmark> findByUserId(Long userId);
    Bookmark findByUserIdAndPbctNo(Long userId, String pbctNo);
    void deleteBookmark(Long bookmarkId);
    void deleteByUserIdAndPbctNo(Long userId, String pbctNo);
}
