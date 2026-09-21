package com.ecommerce.fulfillment_service.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    private Long orderId;
    private Long paymentId;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private String userId;
}
