package com.ecommerce.core_service.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentModel {

    private final Long id;
    private final UUID uuid;
    private final Long orderId;
    private final BigDecimal amount;
    private final String status;
    private final String paymentMethod;
    private final String transactionId;
    private final LocalDateTime createdAt;
}
