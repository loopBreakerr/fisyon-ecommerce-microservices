package com.ecommerce.core_service.catalog.product.model;

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
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private Boolean isActive;
    private String sellerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID uuid;
    private List<ProductImageModel> images;
    private Integer stockQuantity;
}
