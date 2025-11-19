package com.ljh.sideproj.mapper;

import com.ljh.sideproj.dto.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    void insertNotification(Notification notification);
    List<Notification> findByUserId(Long userId);
    List<Notification> findUnreadByUserId(Long userId);
    Notification findById(Long notificationId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);
    int countUnreadByUserId(Long userId);
    void deleteAllByUserId(Long userId);
    List<com.ljh.sideproj.dto.Bid> getMyBids(@Param("userId") Long userId);
}
