package com.ecommerce.fulfillment_service.notification.repository;

import com.ecommerce.fulfillment_service.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndReadFalse(String userId);

    Optional<Notification> findByIdAndUserId(Long id, String userId);

    List<Notification> findByUserIdAndReadFalse(String userId);
}
