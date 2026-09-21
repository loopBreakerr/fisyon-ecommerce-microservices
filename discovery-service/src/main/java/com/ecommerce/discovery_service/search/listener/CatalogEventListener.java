package com.ecommerce.discovery_service.search.listener;

import com.ecommerce.discovery_service.search.document.ProductDocument;
import com.ecommerce.discovery_service.search.event.ProductChangedEvent;
import com.ecommerce.discovery_service.search.repository.ProductSearchRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventListener {

    private final ProductSearchRepository productSearchRepository;

    public CatalogEventListener(ProductSearchRepository productSearchRepository) {
        this.productSearchRepository = productSearchRepository;
    }

    @KafkaListener(topics = "catalog-events", groupId = "search-service-group",
            containerFactory = "catalogEventsContainerFactory")
    public void handleProductChanged(ProductChangedEvent event) {
        ProductDocument document = new ProductDocument();
        document.setId(String.valueOf(event.getProductId()));
        document.setName(event.getName());
        document.setDescription(event.getDescription());
        document.setPrice(event.getPrice());
        document.setCategoryId(event.getCategoryId());
        document.setSku(event.getSku());
        document.setIsActive(event.getIsActive());

        productSearchRepository.save(document);

        System.out.println("Indexed product " + event.getProductId() + " in Elasticsearch");
    }
}
