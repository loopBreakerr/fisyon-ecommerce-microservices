package com.ecommerce.core_service.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "sku")
    private String sku;

    @Column(name = "quantity_available")
    private Integer quantityAvailable;

    @Column(name = "quantity_reserved")
    private Integer quantityReserved;

    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "uuid")
    private UUID uuid;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        if (this.quantityReserved == null) {
            this.quantityReserved = 0;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
