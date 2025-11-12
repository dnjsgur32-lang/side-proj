package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    User findByUsername(String username);
    User findById(Long userId);
}