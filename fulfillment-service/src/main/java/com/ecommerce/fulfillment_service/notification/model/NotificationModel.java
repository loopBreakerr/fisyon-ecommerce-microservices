package com.ecommerce.fulfillment_service.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationModel {

    private Long id;
    private UUID uuid;
    private String userId;
    private String type;
    private String subject;
    private String body;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private boolean read;
}
