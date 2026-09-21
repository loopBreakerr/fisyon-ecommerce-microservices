package com.ecommerce.core_service.inventory.listener;

import com.ecommerce.core_service.catalog.product.event.ProductChangedEvent;
import com.ecommerce.core_service.inventory.entity.InventoryItem;
import com.ecommerce.core_service.inventory.repository.IInventoryItemRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component/**
 * catalog-events topic'ini dinler (ProductEventPublisher tarafindan yayinlanan ProductChangedEvent).
 * Sadece eventType="CREATED" olanlar islenir - yeni bir urun olusturuldugunda, formda seller'in
 * girdigi initialStock ile bir InventoryItem otomatik olusturulur. UPDATED/diger event tipleri
 * no-op gecilir (stok, ayri bir "stok ekleme" akisiyla yonetiliyor, urun guncellemesiyle degil).
 */
public class CatalogEventListener {

    private final IInventoryItemRepository inventoryItemRepository;

    public CatalogEventListener(IInventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @KafkaListener(topics = "catalog-events", groupId = "inventory-catalog-group", containerFactory = "catalogEventsContainerFactory")
    public void handleCatalogEvent(ProductChangedEvent event) {
        if (!"CREATED".equals(event.getEventType())) {
            return;
        }

        if (inventoryItemRepository.findByProductId(event.getProductId()).isPresent()) {
            // Ayni event tekrar islenirse (kafka en-az-bir-kez teslimati) ikinci bir kayit acilmasin.
            return;
        }

        InventoryItem item = new InventoryItem();
        item.setProductId(event.getProductId());
        item.setSku(event.getSku());
        item.setQuantityAvailable(event.getInitialStock());
        item.setWarehouseLocation("MAIN");
        inventoryItemRepository.save(item);
    }
}
