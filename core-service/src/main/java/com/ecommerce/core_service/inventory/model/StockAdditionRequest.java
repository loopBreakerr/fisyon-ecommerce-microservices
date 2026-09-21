package com.ecommerce.core_service.inventory.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Seller'in kendi urununun stogunu arttirmak icin gonderdigi istek govdesi.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockAdditionRequest{

    @NotNull(message = "Miktar zorunludur")
    @Positive(message = "Miktar pozitif olmalıdır")
    private Integer quantity;
}
