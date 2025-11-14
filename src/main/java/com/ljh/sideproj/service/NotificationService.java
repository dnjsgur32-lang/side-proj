package com.ljh.sideproj.service;

import com.ljh.sideproj.dto.Notification;
import com.ljh.sideproj.dto.UserAlert;
import com.ljh.sideproj.mapper.NotificationMapper;
import com.ljh.sideproj.mapper.UserAlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserAlertMapper userAlertMapper;

    public void createNotification(Long userId, String pbctNo, String type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setPbctNo(pbctNo);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setIsRead(false);
        notificationMapper.insertNotification(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.findUnreadByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    public void  markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId);
    }

    public void markAllAsRead(long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    public void deleteNotification(Long notificationId) {
        notificationMapper.deleteNotification(notificationId);
    }

    public void createAlert(UserAlert alert) {
        userAlertMapper.insertAlert(alert);
    }

    public List<UserAlert> getUserAlerts(Long userId) {
        return userAlertMapper.findByUserId(userId);
    }

    public void updateAlert(UserAlert alert) {
        userAlertMapper.updateAlert(alert);
    }

    public void deleteAlert(Long alertId) {
        userAlertMapper.deleteAlert(alertId);
    }

    public List<UserAlert> getActiveAlerts() {
        return userAlertMapper.findActiveAlerts();
    }

    public void deleteAllNotifications(Long userId) {
        notificationMapper.deleteAllByUserId(userId);
    }
}
