package com.ecommerce.core_service.catalog.product.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ProductChangedEvent event) {
        kafkaTemplate.send("catalog-events", String.valueOf(event.getProductId()), event);
    }
}
