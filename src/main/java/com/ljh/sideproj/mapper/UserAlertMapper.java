package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.UserAlert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAlertMapper {
    void insertAlert(UserAlert alert);
    List<UserAlert> findByUserId(Long userId);
    UserAlert findById(Long alertId);
    void updateAlert(UserAlert alert);
    void deleteAlert(Long alertId);
    List<UserAlert> findActiveAlerts();
}
