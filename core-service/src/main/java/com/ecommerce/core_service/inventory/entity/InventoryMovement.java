package com.ecommerce.core_service.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_item_id")
    private Long inventoryItemId;

    @Column(name = "movement_type")
    private String movementType;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "reference_order_id")
    private Long referenceOrderId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "uuid")
    private UUID uuid;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
    }
}
