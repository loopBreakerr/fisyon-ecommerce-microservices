package com.ecommerce.core_service.catalog.product.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductChangedEvent {
    private Long productId;
    private String uuid;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String sku;
    private Boolean isActive;
    private String sellerId;
    private String eventType;
    private Integer initialStock;
}
