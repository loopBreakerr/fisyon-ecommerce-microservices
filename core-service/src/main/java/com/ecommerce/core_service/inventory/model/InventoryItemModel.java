package com.ecommerce.core_service.inventory.model;

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
public class InventoryItemModel {

    private Long id;
    private UUID uuid;
    private Long productId;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private String warehouseLocation;
    private LocalDateTime updatedAt;

}
