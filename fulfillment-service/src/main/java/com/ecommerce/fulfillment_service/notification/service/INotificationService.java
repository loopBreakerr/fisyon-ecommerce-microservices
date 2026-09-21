package com.ecommerce.fulfillment_service.notification.service;

import com.ecommerce.fulfillment_service.notification.model.NotificationModel;

import java.util.List;

public interface INotificationService {

    List<NotificationModel> getMyNotifications(String userId);

    long getUnreadCount(String userId);

    void markAsRead(Long id, String userId);

    void markAllAsRead(String userId);
}
