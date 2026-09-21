package com.ecommerce.core_service.inventory.repository;

import com.ecommerce.core_service.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IInventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductId(Long productId);

    // ProductServiceImpl.toModel(...) urun listelerini toplu doldururken kullanir - tek bir
    // sorguyla N+1'i onler (bkz. getAllProducts/getProductsByCategory/getMyProducts/getProductById).
    List<InventoryItem> findByProductIdIn(List<Long> productIds);
}
