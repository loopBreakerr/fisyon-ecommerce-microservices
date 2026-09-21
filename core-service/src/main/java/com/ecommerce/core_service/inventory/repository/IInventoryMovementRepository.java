package com.ecommerce.core_service.inventory.repository;

import com.ecommerce.core_service.inventory.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IInventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventoryItemId(Long inventoryItemId);
}
