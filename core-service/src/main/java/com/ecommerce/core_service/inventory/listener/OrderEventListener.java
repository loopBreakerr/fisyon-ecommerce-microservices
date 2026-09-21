package com.ecommerce.core_service.inventory.listener;

import com.ecommerce.core_service.inventory.entity.InventoryItem;
import com.ecommerce.core_service.inventory.entity.InventoryMovement;
import com.ecommerce.core_service.inventory.event.OrderCreatedEvent;
import com.ecommerce.core_service.inventory.repository.IInventoryItemRepository;
import com.ecommerce.core_service.inventory.repository.IInventoryMovementRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("inventoryOrderEventListener")
public class OrderEventListener {

    private final IInventoryItemRepository inventoryItemRepository;
    private final IInventoryMovementRepository inventoryMovementRepository;

    public OrderEventListener(IInventoryItemRepository inventoryItemRepository,
                              IInventoryMovementRepository inventoryMovementRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group", containerFactory = "inventoryOrderEventsContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        for (OrderCreatedEvent.OrderCreatedEventItem item : event.getItems()) {
            // 1. Stok kaydini bul
            InventoryItem inventoryItem = inventoryItemRepository.findByProductId(item.getProductId())
                    .orElseGet(() -> {
                        InventoryItem newItem = new InventoryItem();
                        newItem.setProductId(item.getProductId());
                        newItem.setSku(null);
                        newItem.setQuantityAvailable(100);
                        newItem.setWarehouseLocation("MAIN");
                        return newItem;
                    });

            // 2. quantityAvailable'dan dus, 0'in altina inmesin
            int remaining = inventoryItem.getQuantityAvailable() - item.getQuantity();
            if (remaining < 0) {
                System.out.println("WARNING: Insufficient stock for product " + item.getProductId());
                remaining = 0;
            }
            inventoryItem.setQuantityAvailable(remaining);

            // 3. Kaydet
            inventoryItem = inventoryItemRepository.save(inventoryItem);

            // 4. InventoryMovement kaydi olustur
            InventoryMovement movement = new InventoryMovement();
            movement.setInventoryItemId(inventoryItem.getId());
            movement.setMovementType("OUT");
            movement.setQuantity(item.getQuantity());
            movement.setReferenceOrderId(event.getOrderId());
            inventoryMovementRepository.save(movement);

            // 5. Konsola log
            System.out.println("Stock updated for product " + item.getProductId() + ": -" + item.getQuantity()
                    + ", remaining: " + inventoryItem.getQuantityAvailable());
        }
    }
}
