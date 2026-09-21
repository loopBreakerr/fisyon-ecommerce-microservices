package com.ecommerce.core_service.catalog.category.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQueryModel {

    @NotBlank(message = "Kategori adı zorunludur")
    private String name;
    // Kok kategori icin null olabilir, bu yuzden zorunlu tutulmuyor.
    private Long parentCategoryId;

}
