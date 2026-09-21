package com.ecommerce.fulfillment_service.notification.listener;

import com.ecommerce.fulfillment_service.notification.entity.Notification;
import com.ecommerce.fulfillment_service.notification.event.OrderCreatedEvent;
import com.ecommerce.fulfillment_service.notification.event.PaymentCompletedEvent;
import com.ecommerce.fulfillment_service.notification.keycloak.KeycloakAdminClient;
import com.ecommerce.fulfillment_service.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Component("notificationEventListener")
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    public NotificationEventListener(NotificationRepository notificationRepository,
                                     KeycloakAdminClient keycloakAdminClient) {
        this.notificationRepository = notificationRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group",
            containerFactory = "orderEventsContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 1. siparisi veren musteriye bildirim
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType("EMAIL");
        notification.setSubject("Siparişiniz Alındı");
        notification.setBody("Sipariş #" + event.getOrderId() + " alındı, toplam: " + event.getTotalAmount() + " TL");
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        System.out.println("Notification sent to user " + event.getUserId() + ": Order " + event.getOrderId() + " received");

        // 2. Siparisteki urunlerin sahibi olan seller'lara bildirim - ayni siparişte
        // ayni seller'a ait birden fazla urun varsa TEK bildirim gitsin diye
        // distinct sellerId kullaniliyor.
        if (event.getItems() != null) {
            Set<String> sellerIds = new LinkedHashSet<>();
            for (OrderCreatedEvent.OrderCreatedEventItem item : event.getItems()) {
                if (item.getSellerId() != null) {
                    sellerIds.add(item.getSellerId());
                }
            }

            for (String sellerId : sellerIds) {
                Notification sellerNotification = new Notification();
                sellerNotification.setUserId(sellerId);
                sellerNotification.setType("NEW_ORDER_SELLER");
                sellerNotification.setSubject("Yeni Sipariş");
                sellerNotification.setBody("Sipariş #" + event.getOrderId() + " içinde ürününüz için yeni bir sipariş alındı.");
                sellerNotification.setStatus("SENT");
                sellerNotification.setSentAt(LocalDateTime.now());
                notificationRepository.save(sellerNotification);
            }
        }

        // 3. Sistemdeki tum admin'lere bildirim (Keycloak'tan, 5dk cache'li).
        for (String adminId : keycloakAdminClient.getAdminUserIds()) {
            Notification adminNotification = new Notification();
            adminNotification.setUserId(adminId);
            adminNotification.setType("NEW_ORDER_ADMIN");
            adminNotification.setSubject("Yeni Sipariş");
            adminNotification.setBody("Sipariş #" + event.getOrderId() + " oluşturuldu, toplam: " + event.getTotalAmount() + " TL");
            adminNotification.setStatus("SENT");
            adminNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(adminNotification);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group",
            containerFactory = "paymentEventsContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType("EMAIL");

        boolean success = "SUCCESS".equals(event.getStatus());
        notification.setSubject(success ? "Ödemeniz Başarılı" : "Ödemeniz Başarısız");
        notification.setBody("Sipariş #" + event.getOrderId() + " için ödemeniz "
                + (success ? "başarıyla tamamlandı." : "başarısız oldu.")
                + " Tutar: " + event.getAmount() + " TL");
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        System.out.println("Notification sent: Payment " + event.getStatus() + " for order " + event.getOrderId());
    }
}
