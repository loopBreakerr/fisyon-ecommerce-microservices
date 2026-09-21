package com.ecommerce.fulfillment_service.notification.service.impl;

import com.ecommerce.fulfillment_service.notification.entity.Notification;
import com.ecommerce.fulfillment_service.notification.model.NotificationModel;
import com.ecommerce.fulfillment_service.notification.repository.NotificationRepository;
import com.ecommerce.fulfillment_service.notification.service.INotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationModel> getMyNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(Long id, String userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationModel toModel(Notification notification) {
        return new NotificationModel(
                notification.getId(),
                notification.getUuid(),
                notification.getUserId(),
                notification.getType(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
