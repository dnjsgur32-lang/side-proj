package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void insertUser(User user);

    User findByEmail(String email);

    User findByUsername(String username);
}
