package com.ecommerce.core_service.catalog.product.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageModel {
    private Long id;
    private UUID uuid;
    private Long productId;
    private String contentType;
    private Integer displayOrder;
}
