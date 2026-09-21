package com.ecommerce.fulfillment_service.notification.listener;

import com.ecommerce.fulfillment_service.notification.entity.Notification;
import com.ecommerce.fulfillment_service.notification.event.ProductChangedEvent;
import com.ecommerce.fulfillment_service.notification.keycloak.KeycloakAdminClient;
import com.ecommerce.fulfillment_service.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * catalog-events topic'ini dinler (ProductServiceImpl tarafindan yayinlanan
 * ProductChangedEvent - CREATED/UPDATED/DELETED). Seller'in urun CRUD islemlerini
 * sistemdeki tum admin'lere bildirir (KeycloakAdminClient.getAdminUserIds() -
 * NotificationEventListener.handleOrderCreated()'te zaten kullanilan aynı desen).
 */
@Component
public class CatalogEventListener {

    private final NotificationRepository notificationRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    public CatalogEventListener(NotificationRepository notificationRepository,
                                KeycloakAdminClient keycloakAdminClient) {
        this.notificationRepository = notificationRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @KafkaListener(topics = "catalog-events", groupId = "notification-catalog-group",
            containerFactory = "catalogEventsContainerFactory")
    public void handleCatalogEvent(ProductChangedEvent event) {
        String subject = switch (event.getEventType()) {
            case "CREATED" -> "Yeni Ürün Eklendi";
            case "UPDATED" -> "Ürün Güncellendi";
            case "DELETED" -> "Ürün Silindi";
            default -> null;
        };

        if (subject == null) {
            return;
        }

        String body = switch (event.getEventType()) {
            case "CREATED" -> "Yeni ürün eklendi: " + event.getName() + " (satıcı: " + event.getSellerId() + ")";
            case "UPDATED" -> "Ürün güncellendi: " + event.getName() + " (satıcı: " + event.getSellerId() + ")";
            case "DELETED" -> "Ürün silindi: " + event.getName() + " (satıcı: " + event.getSellerId() + ")";
            default -> null;
        };

        for (String adminId : keycloakAdminClient.getAdminUserIds()) {
            Notification notification = new Notification();
            notification.setUserId(adminId);
            notification.setType("PRODUCT_" + event.getEventType() + "_ADMIN");
            notification.setSubject(subject);
            notification.setBody(body);
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
}
