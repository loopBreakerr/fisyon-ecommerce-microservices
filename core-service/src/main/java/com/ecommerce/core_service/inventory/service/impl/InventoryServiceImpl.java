package com.ecommerce.core_service.inventory.service.impl;

import com.ecommerce.core_service.inventory.entity.InventoryItem;
import com.ecommerce.core_service.inventory.entity.InventoryMovement;
import com.ecommerce.core_service.inventory.model.InventoryItemModel;
import com.ecommerce.core_service.inventory.repository.IInventoryItemRepository;
import com.ecommerce.core_service.inventory.repository.IInventoryMovementRepository;
import com.ecommerce.core_service.inventory.service.IInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements IInventoryService {

    private final IInventoryItemRepository inventoryItemRepository;
    private final IInventoryMovementRepository inventoryMovementRepository;

    public InventoryServiceImpl(IInventoryItemRepository inventoryItemRepository,
                                 IInventoryMovementRepository inventoryMovementRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Override
    public InventoryItemModel getByProductId(Long productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId).orElseThrow();
        return toModel(item);
    }

    @Override
    public Map<Long, Integer> getStockQuantitiesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> result = new HashMap<>();
        for (InventoryItem item : inventoryItemRepository.findByProductIdIn(productIds)) {
            result.put(item.getProductId(), item.getQuantityAvailable() != null ? item.getQuantityAvailable() : 0);
        }
        return result;
    }

    @Override
    @Transactional("inventoryTransactionManager")
    public InventoryItemModel addStock(Long productId, Integer quantity) {
        // findByProductId(...).orElseGet(...) deseni inventory.listener.OrderEventListener'daki
        // lazy-create ile ayni: kayit yoksa (orn. bu urun daha once hic siparis/CatalogEventListener
        // akisindan gecmemisse) sifirdan olusturuluyor, boylece "eksik inventory_items" durumu
        // stok eklerken de kendini onarabiliyor.
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setProductId(productId);
                    newItem.setQuantityAvailable(0);
                    newItem.setWarehouseLocation("MAIN");
                    return newItem;
                });

        // Null-safe toplama: eski (initialStock alani henuz yokken yayinlanmis) catalog-events
        // kayitlarinin Kafka replay'iyle olusturdugu satirlarda quantity_available NULL olabiliyor
        // (bkz. inventory_items NULL duzeltmesi) - burada NPE atmak yerine NULL'u 0 kabul ediyoruz.
        int currentQuantity = item.getQuantityAvailable() != null ? item.getQuantityAvailable() : 0;
        item.setQuantityAvailable(currentQuantity + quantity);
        InventoryItem saved = inventoryItemRepository.save(item);

        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItemId(saved.getId());
        movement.setMovementType("IN");
        movement.setQuantity(quantity);
        inventoryMovementRepository.save(movement);

        return toModel(saved);
    }

    private InventoryItemModel toModel(InventoryItem item) {
        return new InventoryItemModel(
                item.getId(),
                item.getUuid(),
                item.getProductId(),
                item.getQuantityAvailable(),
                item.getQuantityReserved(),
                item.getWarehouseLocation(),
                item.getUpdatedAt()
        );
    }
}
