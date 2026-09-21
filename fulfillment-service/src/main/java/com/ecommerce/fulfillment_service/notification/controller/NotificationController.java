package com.ecommerce.fulfillment_service.notification.controller;

import com.ecommerce.fulfillment_service.notification.model.NotificationModel;
import com.ecommerce.fulfillment_service.notification.service.INotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/mine")
    public List<NotificationModel> getMyNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.getMyNotifications(jwt.getSubject());
    }

    @GetMapping("/notifications/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("count", notificationService.getUnreadCount(jwt.getSubject()));
    }

    @PatchMapping("/notifications/{id}/read")
    public void markAsRead(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        notificationService.markAsRead(id, jwt.getSubject());
    }

    @PatchMapping("/notifications/read-all")
    public void markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllAsRead(jwt.getSubject());
    }
}
