package com.ecommerce.core_service.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderModel {

    private Long id;
    private UUID uuid;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemModel> items;
    private LocalDateTime createdAt;
}
