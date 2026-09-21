package com.ecommerce.core_service.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderItemModel {

    private Long orderId;
    private String productName;
    private Long productImageId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String customerId;
}
