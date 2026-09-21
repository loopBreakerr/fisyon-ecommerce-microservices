package com.ecommerce.fulfillment_service.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * core-service'in catalog.product.event.ProductChangedEvent'inin minimal bir kopyasi -
 * admin bildirim mesaji icin sadece productId, name, sellerId ve eventType yeterli
 * (price/sku/description/vs. burada kullanilmiyor, bu yuzden kopyalanmadi).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductChangedEvent {
    private Long productId;
    private String name;
    private String sellerId;
    private String eventType;
}
