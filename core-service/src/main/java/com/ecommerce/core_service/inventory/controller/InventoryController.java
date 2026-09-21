package com.ecommerce.core_service.inventory.controller;

import com.ecommerce.core_service.inventory.model.InventoryItemModel;
import com.ecommerce.core_service.inventory.service.IInventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {

    private final IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/product/{productId}")
    public InventoryItemModel getByProductId(@PathVariable Long productId) {
        return inventoryService.getByProductId(productId);
    }
}
