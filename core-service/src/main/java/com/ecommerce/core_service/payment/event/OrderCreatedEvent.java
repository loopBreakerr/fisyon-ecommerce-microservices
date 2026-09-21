package com.ecommerce.core_service.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private UUID uuid;
    private String userId;
    private List<OrderCreatedEventItem> items;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCreatedEventItem {

        private Long productId;
        private Integer quantity;
    }
}
