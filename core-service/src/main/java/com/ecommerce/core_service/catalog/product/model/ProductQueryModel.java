package com.ecommerce.core_service.catalog.product.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryModel {

    @NotNull(message = "Kategori ID zorunludur")
    private Long categoryId;

    @NotBlank(message = "Ürün adı zorunludur")
    private String name;

    private String description;

    @NotNull(message = "Fiyat zorunludur")
    @Positive(message = "Fiyat pozitif olmalıdır")
    private BigDecimal price;

    private String sku;

    @Min(value = 0, message = "Başlangıç stok negatif olamaz")
    private Integer initialStock;
}
