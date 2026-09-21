package com.ecommerce.discovery_service.search.controller;

import com.ecommerce.discovery_service.search.document.ProductDocument;
import com.ecommerce.discovery_service.search.repository.ProductSearchRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {

    private final ProductSearchRepository productSearchRepository;

    public SearchController(ProductSearchRepository productSearchRepository) {
        this.productSearchRepository = productSearchRepository;
    }

    @GetMapping("/search/products")
    public List<ProductDocument> searchProducts(@RequestParam String query) {
        return productSearchRepository.findByNameContainingIgnoreCase(query);
    }
}
