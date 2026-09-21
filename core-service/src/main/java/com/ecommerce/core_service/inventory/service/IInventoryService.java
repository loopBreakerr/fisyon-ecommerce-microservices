package com.ecommerce.core_service.inventory.service;

import com.ecommerce.core_service.inventory.model.InventoryItemModel;

import java.util.List;
import java.util.Map;

public interface IInventoryService {

    InventoryItemModel getByProductId(Long productId);

    /**
     * ProductServiceImpl'in urun listelerine (getAllProducts/getProductsByCategory/getMyProducts/
     * getProductById) stok bilgisini N+1 yapmadan gomebilmesi icin tek sorgulu, null-safe bir
     * toplu bakis. Verilen productId'lerden inventory kaydi olmayanlar (orn. CatalogEventListener
     * henuz Kafka event'ini islememisken hemen sorgulanan yeni bir urun) donen map'te hic
     * bulunmaz - cagiran taraf eksik anahtari 0/null olarak yorumlar, getByProductId() gibi
     * orElseThrow() ile patlamaz.
     */
    Map<Long, Integer> getStockQuantitiesByProductIds(List<Long> productIds);

    /**
     * Bir urunun stogunu arttirir ve bir "IN" tipinde InventoryMovement kaydi olusturur.
     * Cagiran taraf (ProductServiceImpl) urun sahipligini onceden dogrulamis olmalidir -
     * bu metod kendi icinde ayrica bir yetki kontrolu yapmaz (deleteProductById/uploadImage
     * ile ayni desen: checkOwnership cagiran katmanda).
     */
    InventoryItemModel addStock(Long productId, Integer quantity);
}
