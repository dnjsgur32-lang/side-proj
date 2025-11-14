package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.Bookmark;
import com.ljh.sideproj.mapper.BookmarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkMapper bookmarkMapper;

    public void addBookmark(Long userId, String pbctNo) {
        Bookmark existing = bookmarkMapper.findByUserIdAndPbctNo(userId, pbctNo);
        if (existing == null){
            Bookmark bookmark = new Bookmark();
            bookmark.setUserId(userId);
            bookmark.setPbctNo(pbctNo);
            bookmarkMapper.insertBookmark(bookmark);
        }
    }

    public void removeBookmark(Long userId, String pbctNo) {
        bookmarkMapper.deleteByUserIdAndPbctNo(userId, pbctNo);
    }

    public List<Bookmark> getUserBookmarks(Long userId) {
        return bookmarkMapper.findByUserId(userId);
    }

    public boolean isBookmarked(Long userId, String pbctNo) {
        return bookmarkMapper.findByUserIdAndPbctNo(userId, pbctNo) != null;
    }
}
